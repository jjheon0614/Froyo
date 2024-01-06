package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginForm extends AppCompatActivity {

    private ImageButton back;
    private EditText emailEt;
    private EditText passwordEt;
    private Button login;
    private FirebaseAuth mAuth;

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
}