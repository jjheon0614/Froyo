package com.example.froyo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddChatActivity extends AppCompatActivity {
    private String userID;
    private Boolean isSearch = false;
    private RecyclerView userList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> userArrayList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // New chat room properties
    private ArrayList<User> people;
    private Button create_button;
    private EditText search_edit;
    long mNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);

        Intent i_get = getIntent();
        userID = i_get.getStringExtra("userID");
        userList = findViewById(R.id.userList);
        userList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        userList.setLayoutManager(layoutManager);
        userArrayList = new ArrayList();
        ImageButton back_button = findViewById(R.id.back_button);
        search_edit = findViewById(R.id.search_edit);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        create_button = findViewById(R.id.create_button);
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(people.size() > 1){
                    showFormat();
                }
                else{
                    createChatRoom(null);
                    finish();
                }
            }
        });
        getData(null);
    }

    private void getData(String str) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Read Document
        db.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        userArrayList.clear();
                        people = new ArrayList();
                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> dataMap = document.getData();
                            if (dataMap == null) {
                            } else {
                                // Get the chat room ID
                                String userName = (String) dataMap.get("username");
                                if (str == null) {
                                    String image = (String) dataMap.get("imageUrl");
                                    userArrayList.add(new User(userName, image, false));
                                } else {
                                    if (userName.contains(str)) {
                                        String image = (String) dataMap.get("imageUrl");
                                        userArrayList.add(new User(userName, image, false));
                                    }
                                }
                            }
                        }
//                        sortChatRoom();
                        adapter.notifyDataSetChanged();
                    }
                });
        adapter = new UserAdapter(userArrayList, this); // Create adapter
        userList.setAdapter(adapter); // connect adapter to list

        userList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    // Handle item click here
                    if (position != RecyclerView.NO_POSITION) {
                        // Get the clicked item
                        User clickedItem = userArrayList.get(position);
                        clickedItem.isSelected = !clickedItem.isSelected;
                        if (clickedItem.isSelected) {
                            people.add(clickedItem);
                        } else {
                            people.remove(clickedItem);
                        }
                        if (people.size() > 0) {
                            create_button.setVisibility(View.VISIBLE);
                        } else {
                            create_button.setVisibility(View.GONE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }
    public void searchUser(View view) {
        String str = search_edit.getText().toString();
        if (str != null) {
            getData(str);
        }
    }
    public void showFormat() {
        final Dialog formatDialog = new Dialog(this);
        formatDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        formatDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        formatDialog.setContentView(R.layout.create_chat_format);

        ImageButton close_button = formatDialog.findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formatDialog.dismiss();
            }
        });

        Button create_button = formatDialog.findViewById(R.id.create_button);
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText chat_title = formatDialog.findViewById(R.id.chat_title);
                if(chat_title.getText().toString() != null){
                    createChatRoom(chat_title.getText().toString());
                    formatDialog.dismiss();
                }
            }
        });
        Window window = formatDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);

        RecyclerView profileList;
        RecyclerView.Adapter mini_adapter;
        RecyclerView.LayoutManager layoutManager2;

        profileList = formatDialog.findViewById(R.id.profileList);
        profileList.setHasFixedSize(true);
        layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        profileList.setLayoutManager(layoutManager2);

        mini_adapter = new MiniProfileAdapter(people, this); // Create adapter
        profileList.setAdapter(mini_adapter); // connect adapter to list
        mini_adapter.notifyDataSetChanged();

        formatDialog.show();
    }
    public void createChatRoom(String title){
        String documentId ="";
        // Creating document ID depending on chat room type
        if(people.size() == 1){
            documentId = userID + "&" + people.get(0).id;
        }
        else{
            documentId = userID + "&" + title + getTime();
        }
        // Add a document to collection "chats" with id of documentId
        DocumentReference docRef = db.collection("chats").document(documentId);

        // Add fields
        Map<String, Object> data = new HashMap<>();
        data.put("chatID", documentId); // Create field: chatID
        List<Map<String, List<String>>> messageList = new ArrayList<>();
        data.put(documentId, messageList);
        if(people.size() > 2){
            data.put("title", title);
        }
        List<Map<String, Integer>> userList = new ArrayList<>();

        for(User u: people){
            Map<String, Integer> user = new HashMap<>();
            user.put(u.id, 0);
            userList.add(user);
        }
        Map<String, Integer> me = new HashMap<>();
        me.put(userID, 0);
        userList.add(me);
        data.put("users", userList);

        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    private String getTime() {
        mNow = System.currentTimeMillis();
        return String.valueOf(mNow);
    }
}