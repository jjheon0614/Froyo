package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ChatListActivity extends AppCompatActivity {

    private String userID, email;
    private Boolean isSearch = false;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private RecyclerView chatRoomList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageButton goToPost, goToPosting, goToProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatRoomList = findViewById(R.id.chatRoomList);
        chatRoomList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        chatRoomList.setLayoutManager(layoutManager);
        chatRoomArrayList = new ArrayList();
        // get intent
        Intent i = getIntent();
        userID = i.getStringExtra("userId");
        email = i.getStringExtra("email");
        getData();



        goToProfile = findViewById(R.id.goToProfile);
        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Read Document
        db.collection("chats")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        chatRoomArrayList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> dataMap = document.getData();
                            if(dataMap == null){
                            }
                            else{
                                // Get the users list
                                List<Map<String, Number>> usersList = (List<Map<String, Number>>) dataMap.get("users");
                                // Get the chat room ID
                                String chatID = (String) dataMap.get("chatID");
                                List<Map<String, List<String>>> currentMessageList = (List<Map<String, List<String>>>) dataMap.get(chatID);
                                // Get the message List
                                int numMessage = currentMessageList.size();
                                //Get the number of people in a chat room
                                int peopleNum = usersList.size();
                                List<String> users = new ArrayList<>();
                                List<Number> checks = new ArrayList<>();

                                for(Map<String, Number> user : usersList){
                                    Collection<String> keys = user.keySet();
                                    users.addAll(new ArrayList<>(keys));

                                    Collection<Number> values = user.values();
                                    checks.addAll(new ArrayList<>(values));
                                }

                                // Get the values for making a low of chatRoomList
                                if(users.contains(userID)){
                                    int myIdx = users.indexOf(userID);
                                    int checkedMessage = checks.get(myIdx).intValue();
                                    int nonCheckedMessage = numMessage - checkedMessage;
                                    String chatRoomTitle;
                                    if(users.size() == 2){
                                        users.remove(userID);
                                        chatRoomTitle = users.get(0);
                                    }
                                    else{
                                        // Get the title of chat room
                                        chatRoomTitle = (String) dataMap.get("title");
                                    }

                                    if(isSearch){
                                        EditText search_edit = findViewById(R.id.search_edit);
                                        String searchText = search_edit.getText().toString();
                                        if(users.contains(searchText) || chatRoomTitle.contains(searchText)){
                                            // Find the last Message of the chatting room
                                            String lastMessage = "";
                                            String lastTime = "";
                                            if (currentMessageList != null && !currentMessageList.isEmpty()) {
                                                Map<String, List<String>> lastMessageMap = currentMessageList.get(currentMessageList.size() - 1);
                                                // Get the values in map in array
                                                Collection<List<String>> values = lastMessageMap.values();
                                                for (List<String> valueList : values) {
                                                    String[] array = valueList.toArray(new String[1]);
                                                    // Get the last value of valuesArray (Get the last message, Get the last time)
                                                    if(array[0].contains("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%")){
                                                        lastMessage = "Emoji";
                                                    }
                                                    else if(array[0].contains("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/images%")){
                                                        lastMessage = "Image";
                                                    }
                                                    else{
                                                        lastMessage = array[0];
                                                    }
                                                    lastTime = array[1];
                                                }
                                            } else {
                                                lastMessage = "No Message";
                                            }
                                            // Adding chat room to array list
                                            chatRoomArrayList.add(new ChatRoom(chatRoomTitle, lastMessage, chatID, peopleNum,
                                                    nonCheckedMessage, lastTime));
                                        }
                                    }
                                    else{
                                        // Find the last Message of the chatting room
                                        String lastMessage = "";
                                        String lastTime = "";
                                        if (currentMessageList != null && !currentMessageList.isEmpty()) {
                                            Map<String, List<String>> lastMessageMap = currentMessageList.get(currentMessageList.size() - 1);
                                            // Get the values in map in array
                                            Collection<List<String>> values = lastMessageMap.values();
                                            for (List<String> valueList : values) {
                                                String[] array = valueList.toArray(new String[1]);
                                                // Get the last value of valuesArray (Get the last message, Get the last time)
                                                if(array[0].contains("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/emoji%")){
                                                    lastMessage = "Emoji";
                                                }
                                                else if(array[0].contains("https://firebasestorage.googleapis.com/v0/b/assignment3-login-e1207.appspot.com/o/images%")){
                                                    lastMessage = "Image";
                                                }
                                                else{
                                                    lastMessage = array[0];
                                                }
                                                lastTime = array[1];
                                            }
                                        } else {
                                            lastMessage = "No Message";
                                        }
                                        // Adding chat room to array list
                                        chatRoomArrayList.add(new ChatRoom(chatRoomTitle, lastMessage, chatID, peopleNum,
                                                nonCheckedMessage, lastTime));
                                    }
                                }
                            }


                        }
                        sortChatRoom();
                        adapter.notifyDataSetChanged();
                    }
                });
        adapter = new ChatRoomAdapter(chatRoomArrayList, this); // Create adapter
        chatRoomList.setAdapter(adapter); // connect adapter to list

        chatRoomList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    // Handle item click here
                    if (position != RecyclerView.NO_POSITION) {
                        // Get the clicked item
                        ChatRoom clickedItem = chatRoomArrayList.get(position);

                        // Create an Intent to start the next Activity
                        Intent i_chatRoom = new Intent(ChatListActivity.this, ChatRoomActivity.class);

                        // Add data to the Intent
                        i_chatRoom.putExtra("title", clickedItem.title);
                        i_chatRoom.putExtra("chatID", clickedItem.chatId);
                        i_chatRoom.putExtra("peopleNum", clickedItem.numPeople);
                        i_chatRoom.putExtra("userID", userID);
                        // Start the next Activity
                        startActivity(i_chatRoom);
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
    public void showSearchBar(View view){
        LinearLayout searchBar = findViewById(R.id.searchBar);
        LinearLayout title = findViewById(R.id.title);

        searchBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
        isSearch = true;
    }
    public void cancelSearch(View view){
        LinearLayout searchBar = findViewById(R.id.searchBar);
        LinearLayout title = findViewById(R.id.title);
        EditText search_edit = findViewById(R.id.search_edit);

        search_edit.setText("");
        searchBar.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        isSearch = false;
        chatRoomArrayList.clear();
        adapter.notifyDataSetChanged();
        getData();

    }
    public void search(View view){
        chatRoomArrayList.clear();
        adapter.notifyDataSetChanged();
        getData();
    }
    public void sortChatRoom(){
        for(int i = 0; i < chatRoomArrayList.size() -1; i++){
            for(int j = i+1; j < chatRoomArrayList.size(); j++){
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = mFormat.parse(chatRoomArrayList.get(i).time);
                    date2 = mFormat.parse(chatRoomArrayList.get(j).time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (date1 != null && date2 != null) {
                    long differenceInMillis = date1.getTime() - date2.getTime();
                    if(differenceInMillis < 0){
                        Collections.swap(chatRoomArrayList, i, j);
                    }
                }
                else{
                    Collections.swap(chatRoomArrayList, j, chatRoomArrayList.size()-1);
                }
            }
        }
    }
}