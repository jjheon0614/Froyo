package com.example.froyo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UserList extends AppCompatActivity {
    private ImageButton back, searchBtn;
    private String email;
    private String type;
    private android.widget.Button followingsBtn, followersBtn;
    private ArrayList<String> followingArr;
    private ArrayList<String> followersArr;
    private EditText searchEt;
    private ListUserAdapter adapter;

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

        followingsBtn = findViewById(R.id.buttonFollowings);
        followersBtn = findViewById(R.id.buttonFollowers);


        Intent i = getIntent();
        email = i.getStringExtra("email");
        type = i.getStringExtra("type");

        // To get the ArrayList<String> if it was added as a Serializable extra
        if (i.hasExtra("followingArr")) {
            followingArr = (ArrayList<String>) i.getSerializableExtra("followingArr");
        }

        // To get the ArrayList<String> if it was added as a Serializable extra
        if (i.hasExtra("followersArr")) {
            followersArr = (ArrayList<String>) i.getSerializableExtra("followersArr");
        }


        RecyclerView recyclerView = findViewById(R.id.followList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (type.equals("followers")) {
            followersBtn.setTextColor(getResources().getColor(R.color.blue));
            followersBtn.setBackgroundResource(R.drawable.blue_underline);

            followingsBtn.setTextColor(getResources().getColor(R.color.black));
            followingsBtn.setBackgroundColor(getResources().getColor(android.R.color.white));

            fetchUsersData(followersArr, new OnUserDataFetchedListener() {
                @Override
                public void onUserDataFetched(ArrayList<ListUser> users) {
                    // 'users' contains the ListUser objects with the data fetched from Firestore
                    // Update your RecyclerView adapter here
                    adapter = new ListUserAdapter(getApplicationContext(), users);
                    recyclerView.setAdapter(adapter);
                }
            });
        } else {
            fetchUsersData(followingArr, new OnUserDataFetchedListener() {
                @Override
                public void onUserDataFetched(ArrayList<ListUser> users) {
                    // 'users' contains the ListUser objects with the data fetched from Firestore
                    // Update your RecyclerView adapter here
                    adapter = new ListUserAdapter(getApplicationContext(), users);
                    recyclerView.setAdapter(adapter);
                }
            });
        }

        followingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followingsBtn.setTextColor(getResources().getColor(R.color.blue));
                followingsBtn.setBackgroundResource(R.drawable.blue_underline);

                followersBtn.setTextColor(getResources().getColor(R.color.black));
                followersBtn.setBackgroundColor(getResources().getColor(android.R.color.white));

                fetchUsersData(followingArr, new OnUserDataFetchedListener() {
                    @Override
                    public void onUserDataFetched(ArrayList<ListUser> users) {
                        // 'users' contains the ListUser objects with the data fetched from Firestore
                        // Update your RecyclerView adapter here
                        adapter = new ListUserAdapter(getApplicationContext(), users);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });

        followersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followersBtn.setTextColor(getResources().getColor(R.color.blue));
                followersBtn.setBackgroundResource(R.drawable.blue_underline);

                followingsBtn.setTextColor(getResources().getColor(R.color.black));
                followingsBtn.setBackgroundColor(getResources().getColor(android.R.color.white));


                fetchUsersData(followersArr, new OnUserDataFetchedListener() {
                    @Override
                    public void onUserDataFetched(ArrayList<ListUser> users) {
                        // 'users' contains the ListUser objects with the data fetched from Firestore
                        // Update your RecyclerView adapter here
                        adapter = new ListUserAdapter(getApplicationContext(), users);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });


        searchBtn = (ImageButton) findViewById(R.id.searchUserBtn);
        searchEt = (EditText) findViewById(R.id.searchUser);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchUser = searchEt.getText().toString();
                if (adapter != null) { // Ensure adapter is not null
                    adapter.filter(searchUser); // Call the filter method in your adapter
                }
            }
        });
    }

    private void fetchUsersData(ArrayList<String> emailList, final OnUserDataFetchedListener listener) {
        final ArrayList<ListUser> users = new ArrayList<>();
        final AtomicInteger emailsProcessed = new AtomicInteger();

        for (String email : emailList) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String username = document.getString("username");
                                String imageUrl = document.getString("imageUrl");
                                users.add(new ListUser(email, username, imageUrl));
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }

                        // Check if all emails have been processed
                        if (emailsProcessed.incrementAndGet() == emailList.size()) {
                            listener.onUserDataFetched(users);
                        }
                    });
        }
    }


    public interface OnUserDataFetchedListener {
        void onUserDataFetched(ArrayList<ListUser> users);
    }

}