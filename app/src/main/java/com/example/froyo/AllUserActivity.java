package com.example.froyo;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllUserActivity extends AppCompatActivity {
    private ImageButton goToPosting, goToChat, goToProfile, goToPost;
    private String email, userID, userImageUrl;
    private ListUserAdapter adapter;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        Intent i = getIntent();
        userID = i.getStringExtra("userId");
        email = i.getStringExtra("email");
        userImageUrl = i.getStringExtra("imageUrl");

        RecyclerView recyclerView = findViewById(R.id.allUserList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<ListUser> users = new ArrayList<>();

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase Authentication if needed
                FirebaseAuth.getInstance().signOut();

                // Create an intent to start MainActivity
                Intent intent = new Intent(AllUserActivity.this, MainActivity.class);

                // Clear the activity stack and start new activity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Finish current activity
                finish();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String email = document.getString("email");
                            String imageUrl = document.getString("imageUrl");
                            users.add(new ListUser(email, username, imageUrl));
                        }
                        ListUserAdapter adapter = new ListUserAdapter(getApplicationContext(), users);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });



        goToChat = findViewById(R.id.goToChat);
        goToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllUserActivity.this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                intent.putExtra("imageUrl", userImageUrl);
                startActivity(intent);
                finish();
            }
        });

        goToPosting = findViewById(R.id.goToPosting);
        goToPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllUserActivity.this, NewPostForm.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("userId", userID);
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
                Intent intent = new Intent(AllUserActivity.this, PostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                intent.putExtra("imageUrl", userImageUrl);
                startActivity(intent);
                finish();
            }
        });
    }
}