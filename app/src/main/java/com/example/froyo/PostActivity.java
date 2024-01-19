package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private RecyclerView postRecView;
    private String userID, email;
    private ArrayList<Post> postsArrayList = new ArrayList<>();
    private PostListViewAdapter adapter;
    private ImageButton goToProfile, goToPosting, goToChat, goToSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        // get intent
        Intent i = getIntent();
        userID = i.getStringExtra("userId");
        email = i.getStringExtra("email");

        // Initialize RecyclerView and Adapter
        postRecView = findViewById(R.id.postRecView);

        goToProfile = findViewById(R.id.goToProfile);
        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, ProfileActivity.class);
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
                Intent intent = new Intent(PostActivity.this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        goToPosting = findViewById(R.id.goToPosting);
        goToPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, NewPostForm.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        goToSearch = findViewById(R.id.goToSearch);

        goToSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String username = userID;
                intent.putExtra("userId", username);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        if (postRecView != null) {
            adapter = new PostListViewAdapter(this);
            postRecView.setAdapter(adapter);
            postRecView.setLayoutManager(new LinearLayoutManager(this));

            // Fetch data from Firestore
            getData1();
        } else {
            Log.e("PostActivity", "RecyclerView is null");
        }
    }

    private void getData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        postsArrayList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> dataMap = document.getData();
                            if (dataMap != null) {
                                // Parse Firestore document data into a Post object
                                Post post = new Post();
                                post.setId((String) dataMap.get("id"));
                                post.setUserEmail((String) dataMap.get("username"));
                                post.setMajorTag((String) dataMap.get("majorTag"));
                                post.setContent((String) dataMap.get("content"));
                                post.setImages((ArrayList<String>) dataMap.get("images"));
                                post.setHashTag((ArrayList<String>) dataMap.get("hashTag"));
                                post.setLikes(((Long) dataMap.get("likes")).intValue());
                                post.setComments((ArrayList<String>) dataMap.get("comments"));

                                // Add the Post object to the list
                                postsArrayList.add(post);
                            }
                        }

                        // After populating postsArrayList, set it to the adapter
                        adapter.setPosts(postsArrayList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreError", "Error getting documents", e);
                    }
                });
    }

    private void getData1() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("FirestoreError", "Error getting documents", error);
                    return;
                }

                postsArrayList.clear();

                for (QueryDocumentSnapshot document : value) {
                    Map<String, Object> dataMap = document.getData();
                    if (dataMap != null) {
                        // Parse Firestore document data into a Post object
                        Post post = new Post();
                        post.setId((String) dataMap.get("id"));
                        post.setUserEmail((String) dataMap.get("username"));
                        post.setMajorTag((String) dataMap.get("majorTag"));
                        post.setContent((String) dataMap.get("content"));
                        post.setImages((ArrayList<String>) dataMap.get("images"));
                        post.setHashTag((ArrayList<String>) dataMap.get("hashTag"));
                        post.setLikes(((Long) dataMap.get("likes")).intValue());
                        post.setComments((ArrayList<String>) dataMap.get("comments"));

                        // Add the Post object to the list
                        postsArrayList.add(post);
                    }
                }

                // After populating postsArrayList, set it to the adapter
                adapter.setPosts(postsArrayList);
            }
        });
    }
}
