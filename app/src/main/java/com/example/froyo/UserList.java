package com.example.froyo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class UserList extends AppCompatActivity {
    private ImageButton back;
    private String email;
    private String type;
    private android.widget.Button followings, followers;
    private ArrayList<String> followingArr;
    private ArrayList<String> followersArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        back = (ImageButton) findViewById(R.id.backFromUserList);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        followings = findViewById(R.id.buttonFollowings);
        followers = findViewById(R.id.buttonFollowers);


        Intent i = getIntent();
        email = i.getStringExtra("email");
        type = i.getStringExtra("type");

        // To get the ArrayList<String> if it was added as a Serializable extra
        if (i.hasExtra("followingArr")) {
            followingArr = (ArrayList<String>) i.getSerializableExtra("followingArr");
        }

        // To get the ArrayList<String> if it was added as a Serializable extra
        if (i.hasExtra("followersArr")) {
            followingArr = (ArrayList<String>) i.getSerializableExtra("followersArr");
        }


        if (type.equals("followers")) {
            followers.setTextColor(getResources().getColor(R.color.blue));
            followers.setBackgroundResource(R.drawable.blue_underline);

            followings.setTextColor(getResources().getColor(R.color.black));
            followings.setBackgroundColor(getResources().getColor(android.R.color.white));
        }

        followings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followings.setTextColor(getResources().getColor(R.color.blue));
                followings.setBackgroundResource(R.drawable.blue_underline);

                followers.setTextColor(getResources().getColor(R.color.black));
                followers.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followers.setTextColor(getResources().getColor(R.color.blue));
                followers.setBackgroundResource(R.drawable.blue_underline);

                followings.setTextColor(getResources().getColor(R.color.black));
                followings.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        });


    }
}