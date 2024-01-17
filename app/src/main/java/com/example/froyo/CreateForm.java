package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateForm extends AppCompatActivity {

    private ImageButton back;
    private EditText emailEt;
    private EditText passwordEt;
    private EditText passwordConfirmEt;
    private Button create;
    private FirebaseAuth mAuth;
    private SignInButton btn_google;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_form);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        back = (ImageButton) findViewById(R.id.backFromCreate);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateForm.this, MainActivity.class);
                intent.putExtra("createSuccess", "false");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btn_google = findViewById(R.id.btn_google_create);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // When the button is clicked
        btn_google.setOnClickListener(view -> {
            // Sign out from Google before signing in to ensure account picker is always shown
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEt = (EditText) findViewById(R.id.emailText);
                passwordEt = (EditText) findViewById(R.id.passwordText);
                passwordConfirmEt = (EditText) findViewById(R.id.passwordConfirmText);


                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();
                String passwordConfirm = passwordConfirmEt.getText().toString().trim();

//                if (!password.equals(passwordConfirm)) {
//                    // Handle password confirmation failure
//                    passwordConfirmEt.setHint("Passwords do not match"); // Set hint
//                    passwordConfirmEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    passwordConfirmEt.setText(""); // Clear the text
//                    return;
//                } else if (password.length() < 6) {
//                    passwordEt.setHint("Passwords should have at least 6 characters"); // Set hint
//                    passwordEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    passwordEt.setText(""); // Clear the text
//                    return;
//                } else if (email.isEmpty()) {
//                    emailEt.setHint("Please fill out email"); // Set hint
//                    emailEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    emailEt.setText(""); // Clear the text
//                    return;
//                } else if (password.isEmpty()) {
//                    passwordEt.setHint("Please fill out password"); // Set hint
//                    passwordEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    passwordEt.setText(""); // Clear the text
//                    return;
//                } else if (passwordConfirm.isEmpty()) {
//                    passwordConfirmEt.setHint("Please fill out password confirmation"); // Set hint
//                    passwordConfirmEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    passwordConfirmEt.setText(""); // Clear the text
//                    return;
//                } else if (!email.contains("@")) {
//                    emailEt.setHint("Wrong email format"); // Set hint
//                    emailEt.setHintTextColor(Color.RED); // Set hint text color to red
//                    emailEt.setText(""); // Clear the text
//                    return;
//                }

//                mAuth.createUserWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(CreateForm.this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    // Redirect to another activity or show success message
//                                    Toast.makeText(CreateForm.this, "Authentication success.",
//                                            Toast.LENGTH_SHORT).show();
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    // User creation failed
//                                    try {
//                                        throw task.getException();
//                                    } catch(FirebaseAuthUserCollisionException e) {
//                                        // Email already exists
//                                        Toast.makeText(CreateForm.this, "Already in use",
//                                                Toast.LENGTH_SHORT).show();
//                                    } catch(Exception e) {
//                                        // Other errors
//                                        Toast.makeText(CreateForm.this, "Authentication fail.",
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            }
//                        });

                mAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                                if (isNewUser) {
                                    // Email does not exist, proceed to DetailForm
                                    Intent intent = new Intent(CreateForm.this, DetailForm.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("password", password);
                                    intent.putExtra("type", "normal");
                                    startActivityForResult(intent, 300);
                                } else {
                                    // Email already exists
                                    Toast.makeText(CreateForm.this, "Email already in use", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, get the current user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Check if user exists in Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference docRef = db.collection("users").document(firebaseUser.getUid());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (!document.exists()) {
                                                // User does not exist, create a new user document in Firestore
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("email", firebaseUser.getEmail());
                                                docRef.set(user)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // User created in Firestore, now move to DetailForm
                                                            Intent intent = new Intent(CreateForm.this, DetailForm.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            intent.putExtra("email", firebaseUser.getEmail());
                                                            intent.putExtra("name", firebaseUser.getDisplayName());
                                                            intent.putExtra("type", "google");
                                                            startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Handle the error, possibly retry or inform the user
                                                        });
                                            } else {
                                                // User already exists in Firestore, handle according to your logic
                                                Intent intent = new Intent(CreateForm.this, DetailForm.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("email", firebaseUser.getEmail());
                                                intent.putExtra("name", firebaseUser.getDisplayName());
                                                intent.putExtra("type", "google");
                                                startActivity(intent);
                                                finish();
                                                Toast.makeText(CreateForm.this, "You have already created the account", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Task failed with an exception
                                            // Handle the exception
                                        }
                                    }
                                });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras().get("detailSuccess").toString().equals("false")) {
                    Toast.makeText(this, "Back from detail form fail", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Back from detail form", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateForm.this, MainActivity.class);
                    intent.putExtra("createSuccess", "true");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        } else if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Google Sign In was successful, now authenticate with Firebase
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Handle error - display a message to the user, log the error, etc.
            }
        }
    }
}