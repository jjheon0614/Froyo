package com.example.froyo;

import static android.view.View.GONE;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatRoomActivity extends AppCompatActivity {
    String chatID;
    String userID;
    String searchStr = "";
    List<Map<String, List<String>>> currentMessageList;
    List<Integer> foundIdx = new ArrayList();
    int iterator = 0;
    private static final int PICK_IMAGE_REQUEST = 1;
    Map<String, Object> dataMap;
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Boolean isSearch = false;
    private RecyclerView messageList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Message> messageArrayList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        TextView title = findViewById(R.id.title);
        TextView peopleNum = findViewById(R.id.peopleNum);
        ImageButton back_button = findViewById(R.id.back_button);
        EditText message = findViewById(R.id.message);
        LinearLayout messageFinder = findViewById(R.id.messageFinder);

        messageFinder.setVisibility(View.GONE);
        // Get intent from chat list
        Intent i_get = getIntent();
        chatID = i_get.getStringExtra("chatID");
        // Set up the title of the chat room
        title.setText(i_get.getStringExtra("title"));
        if (i_get.getIntExtra("peopleNum", 0) == 2) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) title.getLayoutParams();
            layoutParams.setMargins(80, 65, 0, 0);
            peopleNum.setVisibility(GONE);
        } else {
            peopleNum.setText(i_get.getIntExtra("peopleNum", 0) + " people");
        }
        // Get userID
        userID = i_get.getStringExtra("userID");

        // Add OnClickListener for back_button to go back to the chat list.
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessage(message.getText().toString());
                    message.setText("");
                    return true;
                }
                return false;
            }
        });

        messageList = findViewById(R.id.messageList);
        messageList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        messageList.setLayoutManager(layoutManager);
        messageArrayList = new ArrayList();
        getData();
    }

    private void getData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Read Document
        db.collection("chats")
                .whereEqualTo(FieldPath.documentId(), chatID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("ChatRoomActivity", "Error getting documents", error);
                            return;
                        }
                        messageArrayList.clear();
                        foundIdx.clear();
                        for (QueryDocumentSnapshot document : value) {
                            dataMap = document.getData();
                            currentMessageList = (List<Map<String, List<String>>>) dataMap.get(chatID);
                            // Message check
                            List<Map<String, Integer>> users = (List<Map<String, Integer>>) dataMap.get("users");
                            messageCheck(users, currentMessageList.size());
                            if (isSearch) {
                                EditText search_edit = findViewById(R.id.search_edit);
                                searchStr = search_edit.getText().toString();
                                int index = 0;
                                iterator = 0;
                                for (Map<String, List<String>> message : currentMessageList) {
                                    String content = "", time = "";
                                    Boolean isSearched = false;
                                    Collection<List<String>> values = message.values();
                                    for (List<String> valueList : values) {
                                        String[] array = valueList.toArray(new String[0]);
                                        content = array[0];
                                        if (content.toLowerCase().contains(searchStr.toLowerCase())) {
                                            isSearched = true;
                                            foundIdx.add(index);
                                        }
                                        time = array[1];
                                    }
                                    Collection<String> keys = message.keySet();
                                    String[] keysArray = keys.toArray(new String[0]);

                                    String user = keysArray[0];
                                    Boolean isMine;
                                    if (user.equals(userID)) {
                                        isMine = true;
                                    } else {
                                        isMine = false;
                                    }
                                    // Adding chat room to array list
                                    messageArrayList.add(new Message(isMine, isSearched, content, searchStr, time.substring(11, 16), user));
                                    index++;
                                }
                                if (foundIdx.size() != 0) {
                                    scrollToItem(foundIdx.get(0));
                                } else {
                                    Toast.makeText(ChatRoomActivity.this, "No messages are founded",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                for (Map<String, List<String>> message : currentMessageList) {
                                    String content = "", time = "";
                                    Collection<List<String>> values = message.values();
                                    for (List<String> valueList : values) {
                                        String[] array = valueList.toArray(new String[0]);
                                        content = array[0];
                                        time = array[1];
                                    }
                                    Collection<String> keys = message.keySet();
                                    String[] keysArray = keys.toArray(new String[0]);

                                    String user = keysArray[0];
                                    Boolean isMine;
                                    if (user.equals(userID)) {
                                        isMine = true;
                                    } else {
                                        isMine = false;
                                    }
                                    // Adding chat room to array list
                                    messageArrayList.add(new Message(isMine, false, content, null, time.substring(11, 16), user));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (foundIdx.size() == 0) {
                            scrollToItem(adapter.getItemCount() - 1);
                        }
                    }
                });
        adapter = new MessageAdapter(messageArrayList, this); // Create adapter
        messageList.setAdapter(adapter); // connect adapter to list
    }

    private void sendMessage(String message) {
        List<String> newMessageContent = new ArrayList<>();
        Map<String, List<String>> newMessage = new HashMap<>();
        newMessageContent.add(message);
        newMessageContent.add(getTime());
        newMessage.put(userID, newMessageContent);
        currentMessageList.add(newMessage);
        dataMap.put(chatID, currentMessageList);

        db.collection("chats").document(chatID).set(dataMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ChatRoomActivity", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ChatRoomActivity", "Error updating document", e);
                    }
                });
    }

    private void messageCheck(List<Map<String, Integer>> users, int currentMessageNum) {
        List<String> usersList = new ArrayList<>();
        List<Number> checks = new ArrayList<>();

        for (Map<String, Integer> user : users) {
            Collection<String> keys = user.keySet();
            usersList.addAll(new ArrayList<>(keys));

            Collection<Integer> values = user.values();
            checks.addAll(new ArrayList<>(values));
        }

        int myIdx = usersList.indexOf(userID);
        users.get(myIdx).put(userID, currentMessageNum);

        dataMap.put("users", users);

        db.collection("chats").document(chatID).set(dataMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ChatRoomActivity", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ChatRoomActivity", "Error updating document", e);
                    }
                });
    }

    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
    private void scrollToItem(int position) {
        if (position > 0) {
            messageList.scrollToPosition(position);
        }
    }
    public void showSearchBar(View view) {
        LinearLayout searchBar = findViewById(R.id.searchBar);
        LinearLayout top_bar = findViewById(R.id.top_bar);

        searchBar.setVisibility(View.VISIBLE);
        top_bar.setVisibility(View.GONE);
        isSearch = true;
    }
    public void cancelSearch(View view) {
        LinearLayout searchBar = findViewById(R.id.searchBar);
        LinearLayout top_bar = findViewById(R.id.top_bar);
        EditText search_edit = findViewById(R.id.search_edit);
        LinearLayout messageFinder = findViewById(R.id.messageFinder);

        search_edit.setText("");
        searchBar.setVisibility(View.GONE);
        messageFinder.setVisibility(View.GONE);
        top_bar.setVisibility(View.VISIBLE);
        isSearch = false;
        messageArrayList.clear();
        adapter.notifyDataSetChanged();
        getData();

    }
    public void search(View view) {
        messageArrayList.clear();
        adapter.notifyDataSetChanged();
        getData();
        LinearLayout messageFinder = findViewById(R.id.messageFinder);
        messageFinder.setVisibility(View.VISIBLE);
    }
    public void moveUp(View view) {
        if (iterator > 0) {
            iterator--;
            scrollToItem(foundIdx.get(iterator));
        } else {
            Toast.makeText(ChatRoomActivity.this, "First Message",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void moveDown(View view) {
        if (iterator < foundIdx.size() - 1) {
            iterator++;
            scrollToItem(foundIdx.get(iterator));
        } else {
            Toast.makeText(ChatRoomActivity.this, "Last Message",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void showEmoji(View view) {
        final Dialog emojiDialog = new Dialog(this);
        emojiDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        emojiDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiDialog.setContentView(R.layout.emoji_window);

        ImageButton close_button = emojiDialog.findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiDialog.dismiss();
            }
        });
        Window window = emojiDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.y = 280;
        window.setAttributes(params);

        ImageView emoji1 = emojiDialog.findViewById(R.id.emoji1);
        ImageView emoji2 = emojiDialog.findViewById(R.id.emoji2);
        ImageView emoji3 = emojiDialog.findViewById(R.id.emoji3);
        ImageView emoji4 = emojiDialog.findViewById(R.id.emoji4);
        ImageView emoji5 = emojiDialog.findViewById(R.id.emoji5);
        ImageView emoji6 = emojiDialog.findViewById(R.id.emoji6);
        DrawableImageViewTarget gifImage1 = new DrawableImageViewTarget(emoji1);
        Glide.with(this).load(R.drawable.like).into(gifImage1);
        DrawableImageViewTarget gifImage2 = new DrawableImageViewTarget(emoji2);
        Glide.with(this).load(R.drawable.heart).into(gifImage2);
        DrawableImageViewTarget gifImage3 = new DrawableImageViewTarget(emoji3);
        Glide.with(this).load(R.drawable.happy).into(gifImage3);
        DrawableImageViewTarget gifImage4 = new DrawableImageViewTarget(emoji4);
        Glide.with(this).load(R.drawable.sad).into(gifImage4);
        DrawableImageViewTarget gifImage5 = new DrawableImageViewTarget(emoji5);
        Glide.with(this).load(R.drawable.laugh).into(gifImage5);
        DrawableImageViewTarget gifImage6 = new DrawableImageViewTarget(emoji6);
        Glide.with(this).load(R.drawable.angry).into(gifImage6);
        emoji1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Flike.gif?alt=media&token=4c431904-6391-4806-83f3-3481a1d90bb6");
                emojiDialog.dismiss();
            }
        });
        emoji2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Fheart.gif?alt=media&token=d850ded9-e2d1-4270-9ba4-19338fabbef5");
                emojiDialog.dismiss();
            }
        });
        emoji3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Fhappy.gif?alt=media&token=8a7d93e2-a0c5-4f25-a65c-91e97dec4724");
                emojiDialog.dismiss();
            }
        });
        emoji4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Fsad.gif?alt=media&token=6a25a03a-7487-4a29-84d7-db8933e1fd27");
                emojiDialog.dismiss();
            }
        });
        emoji5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Flaugh.gif?alt=media&token=6a41866e-32a5-426e-b7b2-acd2b53e4521");
                emojiDialog.dismiss();
            }
        });
        emoji6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%2Fangry.gif?alt=media&token=4d8a6635-5f02-4313-912e-05580027440e");
                emojiDialog.dismiss();
            }
        });
        emojiDialog.show();
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
                            String imageUrl = uri.toString();
                            sendMessage(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatRoomActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
