package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ImageButton goToPost, goToPosting, goToChat;
    private RecyclerView postRecView;
    private PostListViewAdapter adapter;
    private ArrayList<Post> postsArrayList = new ArrayList<>();
    String username;
    private String email;

    List<String> followersArr = new ArrayList<>();
    List<String> followingArr = new ArrayList<>();



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
        email = i.getStringExtra("email");

        // Fetch user data from Firestore
        fetchUserData(email);

        // Initialize RecyclerView and Adapter
        postRecView = findViewById(R.id.profile_posts_recyclerview);
        if (postRecView != null) {
            adapter = new PostListViewAdapter(this);
            postRecView.setAdapter(adapter);
            postRecView.setLayoutManager(new LinearLayoutManager(this));

            // Fetch data from Firestore
            getDataForUser(email);
        } else {
            Log.e("PostActivity", "RecyclerView is null");
        }


        userFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UserList.class);
                intent.putExtra("email", email);
                intent.putExtra("type", "followers");
                intent.putExtra("followingArr", (Serializable) followingArr);
                intent.putExtra("followersArr", (Serializable) followersArr);
                startActivity(intent);
            }
        });

        userFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UserList.class);
                intent.putExtra("email", email);
                intent.putExtra("type", "following");
                intent.putExtra("followingArr", (Serializable) followingArr);
                intent.putExtra("followersArr", (Serializable) followersArr);
                startActivity(intent);
            }
        });



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
                postRecView.setVisibility(View.VISIBLE);
                editLinear.setVisibility(View.GONE);
                posts.setTextColor(getResources().getColor(R.color.blue));
                posts.setBackgroundResource(R.drawable.blue_underline);

                editProfile.setTextColor(getResources().getColor(R.color.black));
                editProfile.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        });

        editProfile = findViewById(R.id.buttonEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLinear.setVisibility(View.VISIBLE);
                postRecView.setVisibility(View.GONE);
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



        goToPost = findViewById(R.id.goToPost);
        goToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userId.getText().toString();
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });


        goToChat = findViewById(R.id.goToChat);
        goToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userId.getText().toString();
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        goToPosting = findViewById(R.id.goToPosting);
        goToPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, NewPostForm.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userId.getText().toString();
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
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
    protected void onResume() {
        super.onResume();
        // Call your method here to refresh data
        getDataForUser(email); // make sure 'email' is the correct variable you want to pass
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


    private void getDataForUser(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'posts' collection where 'userEmail' field equals the provided userEmail
        db.collection("posts")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FirestoreError", "Error getting documents", error);
                            return;
                        }

                        postsArrayList.clear();

                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> dataMap = document.getData();
                            if (dataMap != null) {
                                // Parse Firestore document data into a Post object
                                Post post = new Post();
                                post.setId((String) dataMap.get("id")); // Assuming 'id' is the document ID
                                post.setUserEmail(userEmail);
                                post.setMajorTag((String) dataMap.get("majorTag"));
                                post.setContent((String) dataMap.get("content"));
                                Object imagesObject = dataMap.get("images");
                                if (imagesObject instanceof List<?>) {
                                    List<String> imagesList = (List<String>) imagesObject;
                                    post.setImages(new ArrayList<>(imagesList));  // Set images as an ArrayList
                                } else if (imagesObject instanceof String) {
                                    String singleImageUrl = (String) imagesObject;
                                    ArrayList<String> imagesList = new ArrayList<>();
                                    imagesList.add(singleImageUrl);  // Add single image URL to the list
                                    post.setImages(imagesList);  // Set images as an ArrayList with a single item
                                }
//                                post.setImages((ArrayList<String>) dataMap.get("images"));
                                post.setHashTag((ArrayList<String>) dataMap.get("hashTag"));
                                post.setLikes(((Long) dataMap.get("likes")).intValue());
                                post.setComments((ArrayList<String>) dataMap.get("comments"));

                                // Add the Post object to the list
                                postsArrayList.add(post);
                            }
                        }

                        // After populating postsArrayList, set it to the adapter
                        adapter.setPosts(postsArrayList);
                        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
                    }
                });
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
                            username = documentSnapshot.getString("username");
                            String description = documentSnapshot.getString("description");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            long posts = documentSnapshot.getLong("posts");
                            long followers = documentSnapshot.getLong("followers");
                            long following = documentSnapshot.getLong("following");

                            followersArr = (List<String>) documentSnapshot.get("followersArr");
                            followingArr = (List<String>) documentSnapshot.get("followingArr");

                            userId.setText(username);
                            userDescription.setText(description);
                            userPosts.setText(posts + "\nposts");
                            userFollowers.setText(followers + "\nfollowers");
                            userFollowing.setText(following + "\nfollowing");
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
