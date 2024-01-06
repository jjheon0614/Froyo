package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    private ImageView userImage, editImage;
    private TextView userId, userDescription, userPosts, userFollowers, userFollowing;
    private EditText editId, editDescription;
    private Button logout;
    private android.widget.Button posts;
    private android.widget.Button editProfile;
    private Button imageBtn, saveBtn;
    private Uri selectedImageUri;
    private LinearLayout editLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        userImage = findViewById(R.id.userImage);
        editImage = findViewById(R.id.editImage);
        userId = findViewById(R.id.userId);
        userDescription = findViewById(R.id.userDescription);
        userPosts = findViewById(R.id.userPosts);
        userFollowers = findViewById(R.id.userFollowers);
        userFollowing = findViewById(R.id.userFollowing);
        editId = findViewById(R.id.editId);
        editDescription = findViewById(R.id.editDescription);

        // Get email from intent
        Intent i = getIntent();
        String email = i.getStringExtra("email");

        // Fetch user data from Firestore
        fetchUserData(email);

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase Authentication if needed
                FirebaseAuth.getInstance().signOut();

                // Create an intent to start MainActivity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);

                // Clear the activity stack and start new activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Finish current activity
                finish();
            }
        });


        editLinear = findViewById(R.id.editLinear);
        posts = findViewById(R.id.buttonPosts);
        posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLinear.setVisibility(View.GONE);
                posts.setTextColor(getResources().getColor(R.color.blue));
                posts.setBackgroundResource(R.drawable.blue_underline);

                editProfile.setTextColor(getResources().getColor(R.color.black));
                editProfile.setBackgroundColor(getResources().getColor(android.R.color.white));            }
        });

        editProfile = findViewById(R.id.buttonEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLinear.setVisibility(View.VISIBLE);
                editProfile.setTextColor(getResources().getColor(R.color.blue));
                editProfile.setBackgroundResource(R.drawable.blue_underline);

                posts.setTextColor(getResources().getColor(R.color.black));
                posts.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        });

        imageBtn = (Button) findViewById(R.id.imageBtn);
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to pick an image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*"); // Specify the image MIME data type

                // Start the activity and expect a result with a request code
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE); // Make sure to define this constant in your class
            }
        });

        saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming you've already obtained the updated data
                // and the Uri of the selected image:
                String updatedUsername = editId.getText().toString();
                String updatedDescription = editDescription.getText().toString();


                Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("username", updatedUsername);
                updatedUserData.put("description", updatedDescription);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    // Update Firestore document
                    db.collection("users").document(user.getUid())
                            .update(updatedUserData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Successfully updated user data
                                    // Now upload the image if it's selected
                                    if (selectedImageUri != null) {
                                        uploadImageToStorage(selectedImageUri, email);
                                    }

                                    fetchUserData(email);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle error
                                }
                            });
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from the correct activity and is OK
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Get the image URI
            selectedImageUri = data.getData();
            // Set the image URI to your ImageView using Glide or another image loading library
            Glide.with(this).load(selectedImageUri).into(editImage);
        }
    }

    private void uploadImageToStorage(Uri imageUri, String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the current user's profile from Firestore to retrieve the current image URL
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {
                            // Get the old image URL
                            String oldImageUrl = documentSnapshot.getString("imageUrl");

                            // Delete the old image from Firebase Storage
                            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                                StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    // Old image deleted successfully
                                    // Proceed with uploading the new image
                                    uploadNewImage(imageUri, userEmail);
                                }).addOnFailureListener(e -> {
                                    // Handle the failure of old image deletion
                                });
                            } else {
                                // No existing image, upload the new image
                                uploadNewImage(imageUri, userEmail);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure of fetching user data
                    });
        }
    }

    private void uploadNewImage(Uri imageUri, String userEmail) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child("profiles/" + Uri.encode(userEmail));

        userImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL and update Firestore
                    userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateImageUrlInFirestore(userEmail, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle error in image upload
                });
    }


    // Method to update image URL in Firestore
    private void updateImageUrlInFirestore(String userEmail, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("imageUrl", imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image URL successfully updated in Firestore
                            // Load image using Glide
                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .into(userImage);

                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .into(editImage);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle error
                        }
                    });
        }
    }

    private void fetchUserData(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming email is unique and only one document is returned
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            // Parse data and set to views
                            String username = documentSnapshot.getString("username");
                            String description = documentSnapshot.getString("description");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            long posts = documentSnapshot.getLong("posts");
                            long followers = documentSnapshot.getLong("followers");
                            long following = documentSnapshot.getLong("following");

                            userId.setText(username);
                            userDescription.setText(description);
                            userPosts.setText(posts + "\nFeeds");
                            userFollowers.setText(followers + "\nFollowers");
                            userFollowing.setText(following + "\nFollowing");
                            editId.setText(username);
                            editDescription.setText(description);

                            // Load image using Glide
                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .into(userImage);

                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .into(editImage);


                        } else {
                            // Handle case where user data does not exist
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }

}
