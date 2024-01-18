package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileInfoActivity extends AppCompatActivity {

    private ImageView userImage;
    private TextView userId, userDescription, userPosts, userFollowers, userFollowing;
    private ImageButton back;
    private RecyclerView postRecView;
    private PostListViewAdapter adapter;
    private ArrayList<Post> postsArrayList = new ArrayList<>();
    private android.widget.Button followUser;
    String username;
    String userEmail, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        Intent intent = getIntent();
        if (intent.hasExtra("userEmail")) {
            userEmail = intent.getStringExtra("userEmail");
        }
        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        }

        back = (ImageButton) findViewById(R.id.backFromProfileInfo);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ProfileInfoActivity.this, PostActivity.class);
//                intent.putExtra("backFromInfo", "true");
//                setResult(RESULT_OK, intent);
                finish();
            }
        });

        followUser = findViewById(R.id.followUserInfo);
        followUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Query for the current user
                db.collection("users").whereEqualTo("email", email).limit(1).get().addOnSuccessListener(queryDocumentSnapshotsCurrentUser -> {
                    if (!queryDocumentSnapshotsCurrentUser.isEmpty()) {
                        DocumentSnapshot currentUserSnapshot = queryDocumentSnapshotsCurrentUser.getDocuments().get(0);
                        DocumentReference currentUserDoc = currentUserSnapshot.getReference();
                        List<String> currentFollowingArr = (List<String>) currentUserSnapshot.get("followingArr");

                        // Check if already following
                        boolean isAlreadyFollowing = currentFollowingArr != null && currentFollowingArr.contains(userEmail);

                        // Query for the user being followed/unfollowed
                        db.collection("users").whereEqualTo("email", userEmail).limit(1).get().addOnSuccessListener(queryDocumentSnapshotsFollowedUser -> {
                            if (!queryDocumentSnapshotsFollowedUser.isEmpty()) {
                                DocumentSnapshot followedUserSnapshot = queryDocumentSnapshotsFollowedUser.getDocuments().get(0);
                                DocumentReference followedUserDoc = followedUserSnapshot.getReference();

                                // Start a batch write operation
                                WriteBatch batch = db.batch();

                                if (isAlreadyFollowing) {
                                    // Unfollow
                                    batch.update(followedUserDoc, "followers", FieldValue.increment(-1));
                                    batch.update(followedUserDoc, "followersArr", FieldValue.arrayRemove(email));
                                    batch.update(currentUserDoc, "following", FieldValue.increment(-1));
                                    batch.update(currentUserDoc, "followingArr", FieldValue.arrayRemove(userEmail));
                                    followUser.setText("Follow");
                                } else {
                                    // Follow
                                    batch.update(followedUserDoc, "followers", FieldValue.increment(1));
                                    batch.update(followedUserDoc, "followersArr", FieldValue.arrayUnion(email));
                                    batch.update(currentUserDoc, "following", FieldValue.increment(1));
                                    batch.update(currentUserDoc, "followingArr", FieldValue.arrayUnion(userEmail));
                                    followUser.setText("Following");
                                }

                                // Commit the batch write
                                batch.commit().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Successfully updated followers/following
                                        fetchUserData(userEmail);
                                        Toast.makeText(ProfileInfoActivity.this, isAlreadyFollowing ? "Successfully unfollowed!" : "Successfully followed!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Failed to update followers/following
                                        Toast.makeText(ProfileInfoActivity.this, "Failed to update. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });


        // Initialize views
        userImage = findViewById(R.id.userImageInfo);
        userId = findViewById(R.id.userIdInfo);
        userDescription = findViewById(R.id.userDescriptionInfo);
        userPosts = findViewById(R.id.userPostsInfo);
        userFollowers = findViewById(R.id.userFollowersInfo);
        userFollowing = findViewById(R.id.userFollowingInfo);

        userFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileInfoActivity.this, "hello", Toast.LENGTH_SHORT).show();
            }
        });

        fetchUserData(userEmail);


        postRecView = findViewById(R.id.profile_posts_recyclerview_Info);
        if (postRecView != null) {
            adapter = new PostListViewAdapter(this);
            postRecView.setAdapter(adapter);
            postRecView.setLayoutManager(new LinearLayoutManager(this));

            // Fetch data from Firestore
            getDataForUser(userEmail);
        } else {
            Log.e("PostActivity", "RecyclerView is null");
        }
    }


    private void getDataForUser(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'posts' collection where 'userEmail' field equals the provided userEmail
        db.collection("posts")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                post.setId(userEmail); // Assuming 'id' is the document ID
                                post.setUserEmail(userEmail);
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
                        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
                    }
                });
    }


    private void fetchUserData(String emailUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", emailUser)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming email is unique and only one document is returned
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            List<String> currentFollowersArr = (List<String>) documentSnapshot.get("followersArr");

                            // Check if already following
                            boolean isAlreadyFollowing = currentFollowersArr != null && currentFollowersArr.contains(email);
                            if (isAlreadyFollowing) {
                                followUser.setText("Following");
                            }


                            // Parse data and set to views
                            username = documentSnapshot.getString("username");
                            String description = documentSnapshot.getString("description");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            long posts = documentSnapshot.getLong("posts");
                            long followers = documentSnapshot.getLong("followers");
                            long following = documentSnapshot.getLong("following");

                            userId.setText(username);
                            userDescription.setText(description);
                            userPosts.setText(posts + "\nposts");
                            userFollowers.setText(followers + "\nfollowers");
                            userFollowing.setText(following + "\nfollowing");

                            // Load image using Glide
                            Glide.with(ProfileInfoActivity.this)
                                    .load(imageUrl)
                                    .into(userImage);


                        } else {
                            // Handle case where user data does not exist
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }
}