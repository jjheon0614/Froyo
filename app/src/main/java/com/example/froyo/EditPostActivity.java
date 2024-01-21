package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditPostActivity extends AppCompatActivity {
    private ImageButton back;
    private ImageView editImage;
    private EditText editDescription;
    private android.widget.Button update, delete;
    private String postId;
    private Spinner editMajorTag;
    private String selectedMajorTag, userEmail;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    ArrayAdapter<CharSequence> adapter;
    private LinearLayout hashTagContainer;
    private Uri selectedImageUri = null;
    private static final int REQUEST_CODE_PROFILE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Initialize Firestore and FirebaseStorage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();


        Intent i = getIntent();
        postId = i.getStringExtra("postId");

        // Initialize UI elements
        editImage = findViewById(R.id.editImage);
        editDescription = findViewById(R.id.editPostDescription);
        editMajorTag = findViewById(R.id.editMajorTag);
        update = findViewById(R.id.updateBtn);
        delete = findViewById(R.id.deletePostBtn);
        hashTagContainer = findViewById(R.id.hashTagContainer);


        loadPostData(postId);

        back = (ImageButton) findViewById(R.id.backFromEditPost);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        update = findViewById(R.id.updateBtn);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated data from the UI elements
                String updatedDescription = editDescription.getText().toString();
                String updatedMajorTag = editMajorTag.getSelectedItem().toString();
                Date currentDate = new Date();

                // Collect hashtags from the hashtag container
                ArrayList<String> updatedHashtags = new ArrayList<>();

                // Get the first hashtag if any
//                if(editTextHashtag.getText().toString().trim().length() > 0){
//                    updatedHashtags.add(editTextHashtag.getText().toString().trim());
//                }

                // Iterate through the rest of the hashtagContainer for additional hashtags
                int childCount = hashTagContainer.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = hashTagContainer.getChildAt(i);
                    if (childView instanceof EditText) {
                        String hashtag = ((EditText) childView).getText().toString().trim();
                        if (!hashtag.isEmpty()) {
                            updatedHashtags.add(hashtag);
                        }
                    }
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference postRef = db.collection("posts").document(postId);

                // Prepare the updated post data
                Map<String, Object> updatedPostData = new HashMap<>();
                updatedPostData.put("content", updatedDescription);
                updatedPostData.put("majorTag", updatedMajorTag);
                updatedPostData.put("hashTag", updatedHashtags);
                updatedPostData.put("timestamp", new Timestamp(currentDate));

                // If the image was updated, add the new image url to the updatedPostData
                if (selectedImageUri != null) {
                    // You need to upload the new image and get the URL, similar to uploadNewImage function
                    uploadNewImage(selectedImageUri, userEmail, new ImageUploadCallback() {
                        @Override
                        public void onImageUploaded(String imageUrl) {
                            // Once the image is uploaded and you have the URL, update the imageUrl field

                            ArrayList<String> imageUrls = new ArrayList<>();
                            imageUrls.add(imageUrl); // Add the imageUrl to the list

                            updatedPostData.put("images", imageUrls);

                            // Finally, update the document
                            updatePostDocument(postRef, updatedPostData);
                        }

                        @Override
                        public void onImageUploadFailed(Exception e) {
                            // Handle image upload failure
                        }
                    });
                } else {
                    // No image update, just update the document
                    updatePostDocument(postRef, updatedPostData);
                }
            }
        });

        delete = findViewById(R.id.deletePostBtn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EditPostActivity.this)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked Yes, delete the post
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("posts").document(postId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Optionally, close the activity or navigate the user elsewhere
                                                updateUserPostsInfo();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditPostActivity.this, "Failed to delete post.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", null) // Do nothing on clicking No
                        .show();
            }
        });



        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.major_tags_array,  // Add an array resource in res/values/strings.xml
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        editMajorTag.setAdapter(adapter);

        // Set up the OnItemSelectedListener for the spinner
        editMajorTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item's text
                selectedMajorTag = parentView.getItemAtPosition(position).toString();

                // Do something with the selected major tag (e.g., display it or store it)
                //Toast.makeText(getApplicationContext(), "Selected Major Tag: " + selectedMajorTag, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when nothing is selected (optional)
            }
        });
    }

    // Method to update the post document
    private void updatePostDocument(DocumentReference postRef, Map<String, Object> updatedPostData) {
        postRef.update(updatedPostData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPostActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity or navigate the user elsewhere
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditPostActivity.this, "Failed to update post.", Toast.LENGTH_SHORT).show();
                });
    }

    // Image upload callback interface
    interface ImageUploadCallback {
        void onImageUploaded(String imageUrl);
        void onImageUploadFailed(Exception e);
    }

    // Method to upload a new image and get the URL
    private void uploadNewImage(Uri imageUri, String userEmail, ImageUploadCallback callback) {
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to "userEmail/filename"
        String filename = UUID.randomUUID().toString(); // or use a more relevant name
        StorageReference imageRef = storageRef.child("images/" + userEmail + "/" + filename);

        // Upload file to Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Pass the download URL to the callback
                        callback.onImageUploaded(downloadUri.toString());
                    }).addOnFailureListener(e -> {
                        // Handle any errors
                        callback.onImageUploadFailed(e);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    callback.onImageUploadFailed(e);
                });
    }


    private void updateUserPostsInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                // Get a reference to the user's document

                db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                DocumentReference userDocRef = documentSnapshot.getReference();

                                // Decrement the posts count
                                db.collection("users").document(userDocRef.getId())
                                        .update("posts", FieldValue.increment(-1),
                                                "postsArr", FieldValue.arrayRemove(postId))
                                        .addOnSuccessListener(aVoid -> {
                                            // The posts count was decremented and postId was removed from postsArr successfully

                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure
                                        });
                            } else {
                                // No user found with the provided email
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                        });
            }
        }
    }



    private void loadPostData(String postId) {
        db.collection("posts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Set the description and hashtags
                editDescription.setText(documentSnapshot.getString("content"));
                userEmail = documentSnapshot.getString("userEmail");

                List<String> hashTags = (List<String>) documentSnapshot.get("hashTag");
                if (hashTags != null) {
                    createHashtagEditTexts(hashTags);
                }

                String majorTagFromFirebase = documentSnapshot.getString("majorTag");
                if (majorTagFromFirebase != null) {
                    // Set the spinner to the value you got from Firebase
                    setSpinnerToValue(editMajorTag, adapter, majorTagFromFirebase);
                }

                // For the image, get the URL and then load with Glide
                List<String> images = (List<String>) documentSnapshot.get("images");
                if (images != null && !images.isEmpty()) {
                    // Fetch the first image from the array
                    String imageUrl = images.get(0);
                    // Now use this URL to load the image with Glide
                    Glide.with(EditPostActivity.this)
                            .load(imageUrl)
                            .into(editImage);
                } else {
                    // Handle case where there are no images or images array is empty
                    Toast.makeText(this, "No image available for this post.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Post not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching post data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void createHashtagEditTexts(List<String> hashTags) {
        hashTagContainer.removeAllViews(); // Clear previous views if any
        for (String hashTag : hashTags) {
            EditText hashTagEditText = new EditText(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(40));
            layoutParams.setMargins(0, 0, 0, dpToPx(10)); // Set bottom margin here
            hashTagEditText.setLayoutParams(layoutParams);


            hashTagEditText.setText(hashTag);
            hashTagEditText.setPadding(20, 20, 20, 20); // Example padding, adjust as needed
            hashTagEditText.setBackgroundResource(R.drawable.button_stroke);

            hashTagContainer.addView(hashTagEditText);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    private void setSpinnerToValue(Spinner spinner, ArrayAdapter<CharSequence> adapter, String value) {
        int spinnerPosition = adapter.getPosition(value);
        spinner.setSelection(spinnerPosition);
    }

    public void onProfileImageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PROFILE_IMAGE); // Make sure to define this constant
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ImageView profileImage = findViewById(R.id.editImage);
            profileImage.setImageURI(selectedImageUri);
        }
    }
}