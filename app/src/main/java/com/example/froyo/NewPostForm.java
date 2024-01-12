package com.example.froyo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPostForm extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private String userID, email;
    private EditText editTextPostContent;
    private ImageView imageViewPostPreview;
    private Button buttonAddImage, buttonAddHashtag, buttonSubmitPost;
    private Spinner spinnerMajorTag;
    private LinearLayout hashtagContainer;
    private Uri selectedImageUri;
    private String selectedMajorTag;
    private String currentPostDocument;
    private ImageButton goToPost, goToProfile, goToChat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post_form);

        // get intent
        Intent i = getIntent();
        userID = i.getStringExtra("userId");
        email = i.getStringExtra("email");

        // Initialize components
        editTextPostContent = findViewById(R.id.editTextPostContent);
        imageViewPostPreview = findViewById(R.id.imageViewPostPreview);
        buttonAddImage = findViewById(R.id.buttonAddImage);
        buttonAddHashtag = findViewById(R.id.buttonAddHashtag);
        buttonSubmitPost = findViewById(R.id.buttonSubmitPost);
        spinnerMajorTag = findViewById(R.id.spinnerMajorTag);
        hashtagContainer = findViewById(R.id.hashtagContainer);

        goToProfile = findViewById(R.id.goToProfile);
        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewPostForm.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        goToChat = findViewById(R.id.goToChat);
        goToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewPostForm.this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        // Create an ArrayAdapter using a predefined array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.major_tags_array,  // Add an array resource in res/values/strings.xml
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerMajorTag.setAdapter(adapter);

        // Set up the OnItemSelectedListener for the spinner
        spinnerMajorTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item's text
                selectedMajorTag = parentView.getItemAtPosition(position).toString();

                // Do something with the selected major tag (e.g., display it or store it)
                Toast.makeText(getApplicationContext(),"Selected Major Tag: " + selectedMajorTag, Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when nothing is selected (optional)
            }
        });

        // Set click listener for the "Add Image" button
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to pick an image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*"); // Specify the image MIME data type

                // Start the activity and expect a result with a request code
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE); // Make sure to define this constant in your class
            }
        });

        // Set click listener for the "Add Hashtag" button
        buttonAddHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement logic to add a new hashtag field dynamically
                addHashtagField();
            }
        });

//         Set click listener for the "Submit Post" button
            buttonSubmitPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Assuming you've already obtained the updated data
                    // and the Uri of the selected image:
                    String postContent = editTextPostContent.getText().toString();
                    String majorTag = selectedMajorTag;

                    // Iterate through the hashtagContainer to get hashtag values
                    ArrayList<String> hashtags = new ArrayList<>();
                    int childCount = hashtagContainer.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View childView = hashtagContainer.getChildAt(i);
                        if (childView instanceof LinearLayout) {
                            LinearLayout hashtagLayout = (LinearLayout) childView;
                            if (hashtagLayout.getChildCount() == 2) {
                                View hashtagChildView = hashtagLayout.getChildAt(0);
                                if (hashtagChildView instanceof EditText) {
                                    String hashtag = ((EditText) hashtagChildView).getText().toString().trim();
                                    if (!hashtag.isEmpty()) {
                                        hashtags.add(hashtag);
                                    }
                                }
                            }
                        }
                    }

                    if (selectedImageUri != null) {

                        uploadNewImage(selectedImageUri, email);
                    }

                    // Create a new Post object with the obtained data
                    Post newPost = new Post(
                            // You need to provide appropriate values for these parameters
                            "postId",
                            email,
                            new ArrayList<>(),  // Placeholder for imagesUrl, modify as needed
                            majorTag,
                            hashtags,
                            postContent,
                            0,  // Placeholder for likes, modify as needed
                            new ArrayList<>()  // Placeholder for comments, modify as needed
                    );

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        // Use the user's UID as the document ID for the post
                        db.collection("posts")
                                .add(newPost)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // The post was added successfully
                                        currentPostDocument = documentReference.getId();
                                        // Update the id field of the newPost object with the Firestore document ID
                                        newPost.setId(currentPostDocument);

                                        // Now update the 'id' field in Firestore
                                        documentReference.update("id", currentPostDocument)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Handle success
                                                        Toast.makeText(getApplicationContext(), "Post added with ID: " + currentPostDocument, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle errors here
                                                        Toast.makeText(getApplicationContext(), "Failed to update post ID in Firestore", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle errors here
                                        Toast.makeText(getApplicationContext(), "Post FAILED: ", Toast.LENGTH_SHORT).show();
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
            Glide.with(this).load(selectedImageUri).into(imageViewPostPreview);
        }
    }

    private void addHashtagField() {
        // Create a new LinearLayout to hold the EditText and delete button
        LinearLayout hashtagLayout = new LinearLayout(this);
        hashtagLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a new EditText for the hashtag
        EditText hashtagEditText = new EditText(this);
        hashtagEditText.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Set width to 0 to allow weight to work
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // Set weight to 1 to take available space
        ));
        hashtagEditText.setHint("Enter Hashtag");

        // Create a new delete button (ImageButton with "X" icon)
        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setImageResource(android.R.drawable.ic_delete); // Use the built-in "X" icon
        deleteButton.setBackgroundColor(Color.TRANSPARENT); // Make the background transparent
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the hashtagLayout when the delete button is clicked
                hashtagContainer.removeView(hashtagLayout);
            }
        });

        // Add the EditText and delete button to the LinearLayout
        hashtagLayout.addView(hashtagEditText);
        hashtagLayout.addView(deleteButton);

        // Add the LinearLayout to the hashtagContainer
        hashtagContainer.addView(hashtagLayout);
    }

    private void uploadNewImage(Uri imageUri, String userEmail) {
        // StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        // StorageReference userImageRef = storageRef.child("images/" + Uri.encode(userEmail));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child("images").child(UUID.randomUUID().toString());

        userImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL and update Firestore
                    userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        ArrayList<String> imageUrl = new ArrayList<>();
                        imageUrl.add(uri.toString());
                        updateImageUrlInFirestore(userEmail, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle error in image upload
                });
    }


    // Method to update image URL in Firestore
    private void updateImageUrlInFirestore(String userEmail, ArrayList<String> imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("posts").document(currentPostDocument)
                    .update("images", imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image URL successfully updated in Firestore
                            // Load image using Glide
//                            Glide.with(PostActivity.this)
//                                    .load(imageUrl)
//                                    .into(imageViewPostPreview);
//
//                            Glide.with(PostActivity.this)
//                                    .load(imageUrl)
//                                    .into(imageViewPostPreview);
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


    }