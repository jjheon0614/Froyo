package com.example.froyo;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AddChatActivity extends AppCompatActivity {
    private String userID, imageUrl = null;
    private String groupImage = "https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/profiles%2Fprofile_picture.png?alt=media&token=d4c63757-97e4-4e16-a586-83afc2945c1f";
    private Boolean isSearch = false;
    private Boolean isDocumentExist = false;
    private RecyclerView userList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> userArrayList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;
    private StorageReference storageReference;
    // New chat room properties
    private ArrayList<User> people;
    private Button create_button;
    private EditText search_edit;
    long mNow;
    private static final int PICK_IMAGE_REQUEST = 1;
    private de.hdodenhof.circleimageview.CircleImageView groupImageSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Intent i_get = getIntent();
        userID = i_get.getStringExtra("userID");
        imageUrl = i_get.getStringExtra("imageUrl");
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
                    checkDocumentExist(userID + "&" + people.get(0).id,
                            people.get(0).id + "&" + userID, null);
                    Intent i_create = new Intent(AddChatActivity.this, ChatListActivity.class);
                    setResult(RESULT_OK, i_create);
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
                            }
                            else {
                                // Get the chat room ID
                                String userName = (String) dataMap.get("username");
                                if(!userName.equals(userID)){
                                    if (str == null) {
                                        String image = (String) dataMap.get("imageUrl");
                                        userArrayList.add(new User(userName, image, false));
                                    }
                                    else {
                                        if (userName.contains(str)) {
                                            String image = (String) dataMap.get("imageUrl");
                                            userArrayList.add(new User(userName, image, false));
                                        }
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
                    Intent i_create = new Intent(AddChatActivity.this, ChatListActivity.class);
                    setResult(RESULT_OK, i_create);
                    finish();
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

        groupImageSelect = formatDialog.findViewById(R.id.profile);
        Glide.with(this).load(groupImage).into(groupImageSelect);
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
        String documentId;
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
        if(people.size() > 1){
            data.put("title", title);
            if(groupImage != null){
                data.put("chatRoomImage", groupImage);
            }
        }
        List<Map<String, Object>> userList = new ArrayList<>();
        for(User u: people){
            Map<String, Object> user = new HashMap<>();
            user.put(u.id, Arrays.asList(0, u.image));
            userList.add(user);
        }
        Map<String, Object> me = new HashMap<>();
        me.put(userID, Arrays.asList(0, imageUrl));
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
    public void checkDocumentExist(String id1, String id2, String title) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("chats");

        DocumentReference docRefA = collectionRef.document(id1);
        docRefA.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    Toast.makeText(AddChatActivity.this, "The chat room is already existed 1!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking document a existence", task.getException());
                }
                else{
                    DocumentReference docRefB = collectionRef.document(id2);
                    docRefB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().exists()) {
                                Toast.makeText(AddChatActivity.this, "The chat room is already existed 2!", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error checking document b existence", task.getException());
                            }
                            else{
                                createChatRoom(title);
                            }
                        }
                    });
                }
            }
        });
    }
    private String getTime() {
        mNow = System.currentTimeMillis();
        return String.valueOf(mNow);
    }
    public void openGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            uploadImage(selectedImageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();

            StorageReference imageRef = storageReference.child("images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String image = uri.toString();
                            groupImage = image;
                            Glide.with(this).load(image).into(groupImageSelect);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddChatActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}