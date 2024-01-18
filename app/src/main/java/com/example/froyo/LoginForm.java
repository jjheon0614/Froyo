package com.example.froyo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginForm extends AppCompatActivity {

    private ImageButton back;
    private EditText emailEt;
    private EditText passwordEt;
    private Button login;
    private FirebaseAuth mAuth;
    private SignInButton btn_google;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        back = (ImageButton) findViewById(R.id.backFromLogin);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginForm.this, MainActivity.class);
                intent.putExtra("loginSuccess", "false");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btn_google = findViewById(R.id.btn_google);

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



        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEt = (EditText) findViewById(R.id.loginEmail);
                passwordEt = (EditText) findViewById(R.id.loginPassword);

                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();

                // Check if email is empty
                if(email.isEmpty()) {
                    emailEt.setHint("Please enter your email");
                    emailEt.setHintTextColor(Color.RED);
                    emailEt.setText("");
                    return;
                }

                // Check if password is empty
                if(password.isEmpty()) {
                    passwordEt.setHint("Please enter your password");
                    passwordEt.setHintTextColor(Color.RED);
                    passwordEt.setText("");
                    return;
                }

                // Proceed with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    // Authentication success
                                    Intent intent = new Intent(LoginForm.this, ProfileActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Authentication failure
                                    Toast.makeText(LoginForm.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from Google Sign-In
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
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

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, navigate to ProfileActivity
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = null;
                            if (user != null) {
                                email = user.getEmail(); // Get email from FirebaseUser
                            }
                            Intent intent = new Intent(LoginForm.this, ProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                        }
                    }
                });
    }
}