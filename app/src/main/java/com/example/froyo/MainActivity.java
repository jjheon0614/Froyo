package com.example.froyo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button createBtn;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createBtn = (Button) findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateForm.class);
                intent.putExtra("value", "100");
                startActivityForResult(intent, 100);
            }
        });

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginForm.class);
                intent.putExtra("value", "200");
                startActivityForResult(intent, 200);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras().get("createSuccess").toString().equals("false")) {
                    Toast.makeText(this, "Back from create form fail", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Back from create form", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras().get("loginSuccess").toString().equals("false")) {
                    Toast.makeText(this, "Back from login form fail", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Back from login form", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}