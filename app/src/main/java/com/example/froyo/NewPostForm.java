package com.example.froyo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPostForm extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private TextView tvUsername;
    private String userID, email, userImageUrl;
    private EditText editTextPostContent, editTextHashtag;
    private ImageView imageViewPostPreview, profileImage;
    private ImageButton buttonAddImage, buttonAddHashtag;
    private android.widget.Button buttonSubmitPost;
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
        userImageUrl = i.getStringExtra("imageUrl");

        // Initialize components
        tvUsername = findViewById(R.id.tvUsername);
        editTextPostContent = findViewById(R.id.etContent);
        editTextHashtag = findViewById(R.id.etHashtag);
        imageViewPostPreview = findViewById(R.id.ivPreview);
        buttonAddImage = findViewById(R.id.ibUploadImages);
        buttonAddHashtag = findViewById(R.id.btAddHashtag);
        buttonSubmitPost = findViewById(R.id.btPost);
        spinnerMajorTag = findViewById(R.id.spinnerMajorTag);
        hashtagContainer = findViewById(R.id.llHashtagContainer);
        profileImage = findViewById(R.id.ivProfilePic);

        Glide.with(NewPostForm.this)
                .load(userImageUrl)
                .into(profileImage);

        // Set the username text
        tvUsername.setText(userID);

        goToProfile = findViewById(R.id.goToProfile);
        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (email.equals("admin@gmail.com")) {
                    intent = new Intent(NewPostForm.this, AllUserActivity.class);
                } else {
                    intent = new Intent(NewPostForm.this, ProfileActivity.class);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("email", email);
                intent.putExtra("imageUrl", userImageUrl);
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
                intent.putExtra("imageUrl", userImageUrl);
                startActivity(intent);
                finish();
            }
        });

        goToPost = findViewById(R.id.goToPost);
        goToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewPostForm.this, PostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                intent.putExtra("imageUrl", userImageUrl);
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
                //Toast.makeText(getApplicationContext(),"Selected Major Tag: " + selectedMajorTag, Toast.LENGTH_SHORT ).show();
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
                    Date currentDate = new Date();

                    // Iterate through the hashtagContainer to get hashtag values
                    ArrayList<String> hashtags = new ArrayList<>();

                    // Get the first hashtag if any
                    if(editTextHashtag.getText().toString().trim().length() > 0){
                        hashtags.add(editTextHashtag.getText().toString().trim());
                    }

                    int childCount = hashtagContainer.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View childView = hashtagContainer.getChildAt(i);
                        if (childView instanceof LinearLayout) {
                            LinearLayout hashtagLayout = (LinearLayout) childView;
                            if (hashtagLayout.getChildCount() == 3) {
                                View hashtagChildView = hashtagLayout.getChildAt(1);
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
                            new ArrayList<>(),
                            majorTag,
                            hashtags,
                            postContent,
                            0,
                            new ArrayList<>(),
                            new Timestamp(currentDate)
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
                                                        //Toast.makeText(getApplicationContext(), "Post added with ID: " + currentPostDocument, Toast.LENGTH_SHORT).show();


                                                        DocumentReference userDocRef = db.collection("users").document(user.getUid());

                                                        // Increment the user's posts count
                                                        userDocRef.update("posts", FieldValue.increment(1));

                                                        // Prepare the map to be added to the postsArr
                                                        Map<String, Object> postEntry = new HashMap<>();
                                                        postEntry.put("postId", currentPostDocument);
                                                        postEntry.put("timestamp", new Timestamp(new Date()));

                                                        // Add the new post map to the postsArr
                                                        userDocRef.update("postsArr", FieldValue.arrayUnion(postEntry))
                                                                .addOnSuccessListener(aVoid1 -> Toast.makeText(getApplicationContext(), "User posts updated with ID: " + currentPostDocument, Toast.LENGTH_SHORT).show())
                                                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to update user posts", Toast.LENGTH_SHORT).show());

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
        // Create generic values for padding in dp units
        int dp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        // Create a new LinearLayout to hold the EditText and delete button
        LinearLayout hashtagLayout = new LinearLayout(this);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llParams.bottomMargin = dp10;

        // Set the layout params of the LinearLayout
        hashtagLayout.setLayoutParams(llParams);
        hashtagLayout.setOrientation(LinearLayout.HORIZONTAL);
        hashtagLayout.setGravity(Gravity.CENTER);

        // Create a new ImageView for the hashtag icon
        ImageView ivHashtag = new ImageView(this);

        //Create a new LayoutParams for the ImageView in dp units
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());


        // Set the width, height, marginEnd of the ImageView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMarginEnd(dp10);

        ivHashtag.setLayoutParams(params);
        ivHashtag.setImageResource(R.drawable.hashtag);

        // Create a new EditText for the hashtag
        EditText hashtagEditText = new EditText(this);
        hashtagEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                4.0f // Set weight to 1 to take available space
        ));
        hashtagEditText.setHint("Hashtag");
        hashtagEditText.setBackgroundResource(R.drawable.list_user_stroke);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        hashtagEditText.setPadding(padding,padding,padding,padding);
        hashtagEditText.setEms(12);

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
        hashtagLayout.addView(ivHashtag);
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