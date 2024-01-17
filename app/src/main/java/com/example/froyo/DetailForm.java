package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class DetailForm extends AppCompatActivity {

    private static final int REQUEST_CODE_PROFILE_IMAGE = 1;
    private FirebaseAuth mAuth;
    private TextView passwordTitle;
    private EditText idEt;
    private EditText emailEt;
    private EditText passwordEt;
    private EditText descriptionEt;
    private ImageButton back;
    private Button create;
    private Uri selectedImageUri = null;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_form);


        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String type = intent.getStringExtra("type");

        idEt = (EditText) findViewById(R.id.detailId);
        emailEt = (EditText) findViewById(R.id.detailEmail);
        passwordEt = (EditText) findViewById(R.id.detailPassword);
        descriptionEt = (EditText) findViewById(R.id.detailDescription);
        passwordTitle = (TextView) findViewById(R.id.passwordTitle);

        if (!type.equals("normal")) {
            passwordTitle.setVisibility(View.GONE);
            passwordEt.setVisibility(View.GONE);
        }

        emailEt.setText(email);
        passwordEt.setText(password);

        back = (ImageButton) findViewById(R.id.backFromDetail);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailForm.this, CreateForm.class);
                intent.putExtra("detailSuccess", "false");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        create = (Button) findViewById(R.id.createUser);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("normal")) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(DetailForm.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, get the current user
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            String username = idEt.getText().toString().trim();
                                            String description = descriptionEt.getText().toString().trim();

                                            if(username.isEmpty()) {
                                                Toast.makeText(DetailForm.this, "id cannot be blank", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if(description.isEmpty()) {
                                                Toast.makeText(DetailForm.this, "Description cannot be blank", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Create a user object to upload to Firestore
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("username", username);
                                            user.put("email", email);
                                            user.put("password", password);
                                            user.put("description", description);
                                            user.put("posts", 0);
                                            user.put("following", 0);
                                            user.put("followers", 0);


                                            // Add a new document with a generated ID
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("users").document(firebaseUser.getUid())
                                                    .set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(DetailForm.this, "User data saved.",
                                                                    Toast.LENGTH_SHORT).show();

                                                            uploadImageToStorage(email, selectedImageUri);

                                                            Intent intent = new Intent(DetailForm.this, CreateForm.class);
                                                            intent.putExtra("detailSuccess", "true");
                                                            setResult(RESULT_OK, intent);
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(DetailForm.this, "Error saving user data.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(DetailForm.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Handle the case for non-normal (e.g., Google) authentication
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String email = firebaseUser.getEmail();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("users").document(firebaseUser.getUid());

                        String username = idEt.getText().toString().trim();
                        String description = descriptionEt.getText().toString().trim();

                        // Construct the user data map
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username); // Assuming you want to use the display name as the username
                        user.put("email", firebaseUser.getEmail()); // Get the email from the FirebaseUser
                        user.put("description", description); // Default description
                        user.put("posts", 0); // Default number of posts
                        user.put("following", 0); // Default number of following
                        user.put("followers", 0); // Default number of followers

                        // Update the Firestore document with a merge option
                        db.collection("users").document(firebaseUser.getUid())
                                .set(user, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Document updated successfully
                                        // Proceed with any other operations or navigation
                                        uploadImageToStorage(email, selectedImageUri);

                                        Intent intent = new Intent(DetailForm.this, CreateForm.class);
                                        intent.putExtra("detailSuccess", "true");
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle the error
                                    }
                                });
                    }
                }

            }
        });

    }

    // Method to save or update user data and image
    private void saveUserDataAndImage(DocumentReference docRef, Map<String, Object> userData, Uri imageUri) {
        docRef.set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailForm.this, "User data saved.", Toast.LENGTH_SHORT).show();
                    if (imageUri != null) {
                        uploadImageToStorage(email, imageUri);
                    }

                    // After saving data and image, navigate to the next activity
                    Intent intent = new Intent(DetailForm.this, CreateForm.class); // or any other activity
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailForm.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageToStorage(String email, Uri imageUri) {
        if (imageUri != null) {
            // Initialize Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            // Create a reference to 'profiles/email.jpg'
            StorageReference profileImageRef = storageRef.child("profiles/" + Uri.encode(email) + ".jpg");

            // Put the file in Firebase Storage
            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(DetailForm.this, "Image uploaded successfully.",
                                    Toast.LENGTH_SHORT).show();

                            // If you need the URL of the uploaded image
                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Here you get the image URL
                                    String imageUrl = uri.toString();

                                    // Assuming firebaseUser is your FirebaseUser object
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    if (firebaseUser != null) {
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        // Create a Map to hold the user data to be updated
                                        Map<String, Object> userUpdates = new HashMap<>();
                                        userUpdates.put("imageUrl", imageUrl); // Update the image URL

                                        // Update the user's profile in Firestore
                                        db.collection("users").document(firebaseUser.getUid())
                                                .update(userUpdates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Handle successful update
                                                        Toast.makeText(DetailForm.this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle the error
                                                        Toast.makeText(DetailForm.this, "Error updating profile image.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DetailForm.this, "Image upload failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(DetailForm.this, "No image selected to upload.",
                    Toast.LENGTH_SHORT).show();
        }
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
            ImageView profileImage = findViewById(R.id.profile_image);
            profileImage.setImageURI(selectedImageUri);
        }
    }
}