package com.example.froyo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ProfileInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        Intent intent = getIntent();
        if (intent.hasExtra("userEmail")) {
            String userEmail = intent.getStringExtra("userEmail");
            Toast.makeText(this, userEmail, Toast.LENGTH_SHORT).show();

        }
    }
}