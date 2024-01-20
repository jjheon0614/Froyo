package com.example.froyo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    // Post data
    private ArrayList<Post> allPosts;
    private ArrayList<Post> filteredPosts;

    // Filters and major tag selection
    private final String [] filters = {"Posts", "Major Tags", "Tags", "Date"};
    private String selectedFilter = "", selectedMajorTag = "", searchText = "", tagText = "";
    AutoCompleteTextView filter, actvMajorTag;

    TextInputLayout tilMajorTag;
    ArrayAdapter<String> adapterFilterOptions, adapterMajorTag;

    //Navigation buttons
    ImageButton goBackNewsFeed, ibSearch;

    // Other filters
    TextView tvDateFrom, tvDateTo;
    Button btnDateFrom, btnDateTo;
    LinearLayout llDateFilter;
    EditText etSearchText, etTag;

    // Date range filter
    private Calendar dateFrom, dateTo;
    private DatePickerDialog datePickerDialog;

    // Filtered post display
    private RecyclerView searchRecyclerView;
    private PostListViewAdapter postListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize post arrays
        allPosts = new ArrayList<>();
        filteredPosts = new ArrayList<>();

        // Initialize all components
        filter = findViewById(R.id.actvFilterText);
        goBackNewsFeed = findViewById(R.id.goBackNewsFeed);
        searchRecyclerView = findViewById(R.id.searchRecycleView);
        etSearchText = findViewById(R.id.etSearchText);
        ibSearch = findViewById(R.id.ibSearch);
        etTag = findViewById(R.id.etTags);
        actvMajorTag = findViewById(R.id.actvMajorTag);
        tilMajorTag = findViewById(R.id.tilMajorTag);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        tvDateFrom = findViewById(R.id.tvDateFrom);
        tvDateTo = findViewById(R.id.tvDateTo);
        llDateFilter = findViewById(R.id.llDateFilter);

        // Set visibility to be gone
        llDateFilter.setVisibility(View.GONE);
        etTag.setVisibility(View.GONE);
        tilMajorTag.setVisibility(View.GONE);

        // Get intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String email = intent.getStringExtra("email");

        // Retrieve data from Firestore
        getData();

        // Setup goBackNewsFeed button
        goBackNewsFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, PostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("userId", userId);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        // Setup filter system
        adapterFilterOptions = new ArrayAdapter<>(this, R.layout.list_item, filters);
        filter.setAdapter(adapterFilterOptions);
        filter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedFilter = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(SearchActivity.this, "Selected Filter: " + selectedFilter, Toast.LENGTH_SHORT).show();

                if(selectedFilter.equals("Major Tags")){
                    llDateFilter.setVisibility(View.GONE);
                    etTag.setVisibility(View.GONE);
                    tilMajorTag.setVisibility(View.VISIBLE);
                }
                else if(selectedFilter.equals("Tags")){
                    llDateFilter.setVisibility(View.GONE);
                    tilMajorTag.setVisibility(View.GONE);
                    etTag.setVisibility(View.VISIBLE);
                }
                else if(selectedFilter.equals("Date")){
                    llDateFilter.setVisibility(View.VISIBLE);
                    tilMajorTag.setVisibility(View.GONE);
                    etTag.setVisibility(View.GONE);
                }
                else{
                    llDateFilter.setVisibility(View.GONE);
                    tilMajorTag.setVisibility(View.GONE);
                    etTag.setVisibility(View.GONE);
                }
            }
        });

        // Setup RecyclerView
        postListViewAdapter = new PostListViewAdapter(this);
        searchRecyclerView.setAdapter(postListViewAdapter);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Search system
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFilter.equals("Major Tags")){
                    if(selectedMajorTag.isEmpty()){
                        Toast.makeText(SearchActivity.this, "Please select a major tag", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        searchText = etSearchText.getText().toString();
                        filterMajorTags();
                    }
                }
                else if(selectedFilter.equals("Tags")){
                    if(etTag.getText().toString().isEmpty()){
                        Toast.makeText(SearchActivity.this, "Please enter a tag", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        searchText = etSearchText.getText().toString();
                        tagText = etTag.getText().toString();
                        filterTags();
                    }
                }
                else if(selectedFilter.equals("Date")){
                    if (dateFrom == null || dateTo == null) {
                        Toast.makeText(SearchActivity.this, "Please select a date range", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        searchText = etSearchText.getText().toString();
                        filterDateRange();
                    }
                }
                else { // If SelectedFilter is Posts or by default Posts
                    if(etSearchText.getText().toString().isEmpty()){
                        Toast.makeText(SearchActivity.this, "Please enter a search text", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        searchText = etSearchText.getText().toString();
                        filterPosts();
                    }
                }
            }
        });

        // Setup major tag system
        adapterMajorTag = new ArrayAdapter<>(this, R.layout.list_item, getResources().getStringArray(R.array.major_tags_array));
        actvMajorTag.setAdapter(adapterMajorTag);
        actvMajorTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedMajorTag = adapterView.getItemAtPosition(position).toString();
            }
        });

        // Setup date range system
        btnDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        btnDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });
    }

    private void getData() {
        // Fetch data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("FirestoreError", "Error getting documents", error);
                    return;
                }

                allPosts.clear();

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

                        // Retrieve and set the date field
                        Timestamp timestamp = (Timestamp) dataMap.get("date");
                        if (timestamp != null) {
                            post.setDate(timestamp);
                        }

                        // Add the Post object to the list
                        allPosts.add(post);
                    }
                    else {
                        Log.e("FirestoreError", "Document data is null");
                    }
                }

                // After populating postsArrayList, set it to the adapter
                postListViewAdapter.setPosts(allPosts);
            }
        });
    }

    private void updateFilteredPosts(){
        if(filteredPosts.size() == 0){
            Toast.makeText(SearchActivity.this, "No posts found", Toast.LENGTH_SHORT).show();
            filteredPosts.addAll(allPosts);
        }

        // Update the adapter with the filtered data
        postListViewAdapter = new PostListViewAdapter(this);
        postListViewAdapter.setPosts(filteredPosts);
        searchRecyclerView.setAdapter(postListViewAdapter);

    }

    private void filterPosts() {
        // Filter the data based on the search text
        filteredPosts.clear();

        // Filter the data based on the search text
        filteredPosts = new ArrayList<Post>();
        for (Post aPost : allPosts) {
            if (aPost.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                filteredPosts.add(aPost);
            }
        }
        updateFilteredPosts();
    }

    private void filterMajorTags() {
        // Filter the data based on the search text
        filteredPosts.clear();

        // Filter the data based on the search text and major tag
        if(!searchText.isEmpty()){ // If search text is not empty, filter by both search text and major tag
            for (Post post : allPosts) {
                if (post.getContent().toLowerCase().contains(searchText.toLowerCase()) && post.getMajorTag().contains(selectedMajorTag)) {
                    filteredPosts.add(post);
                }
            }
        }
        else {
            for (Post post : allPosts) { // If search text is not empty, only filter by major tag
                if (post.getMajorTag().contains(selectedMajorTag)) {
                    filteredPosts.add(post);
                }
            }
        }
        updateFilteredPosts();
    }

    private void filterTags() {
        // Filter the data based on the search text
        filteredPosts.clear();
        String [] tags = tagText.split(",");

        // Filter the data based on the search text and tags
        if(!searchText.isEmpty() && tags.length > 0) { // If search text is not empty and tags is not empty, filter by both search text and tags
            for (Post post : allPosts) {
                if (post.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                    checkTagInPost(tags, post);
                }
            }
        }
        // Filter the data based on the tags
        else if (searchText.isEmpty() && tags.length > 0) { // If search text is empty and tags is not empty, filter by tags
            for (Post post : allPosts) {
                checkTagInPost(tags, post);
            }
        }
        updateFilteredPosts();
    }

    private void checkTagInPost(String[] tags, Post post) {
        for (String aTag : tags) {
            if (post.getHashTag().contains(aTag.toLowerCase())) {
                filteredPosts.add(post);
                break;
            }
        }
    }

    private void filterDateRange() {
        // Filter the data based on the search text
        filteredPosts.clear();

        if(!searchText.isEmpty()) { // If search text is not empty, filter by both search text and date range
            for (Post post : allPosts) {
                if (post.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                    checkDateInPost(post);
                }
            }
        }
        else { // If search text is empty, only filter by date range
            for (Post post : allPosts) {
                checkDateInPost(post);
            }
        }

        updateFilteredPosts();
    }

    private void checkDateInPost(Post post) {
        Date postDate = post.getDate().toDate();
        Calendar postCalendar = Calendar.getInstance();
        postCalendar.setTime(postDate);

        if ((postCalendar.compareTo(dateFrom) >= 0) && (postCalendar.compareTo(dateTo) <= 0)) {
            filteredPosts.add(post);
        }
    }

    private void showDatePickerDialog(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (isStartDate) {
                    dateFrom = Calendar.getInstance();
                    dateFrom.set(year, month, dayOfMonth);
                    tvDateFrom.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
                } else {
                    dateTo = Calendar.getInstance();
                    dateTo.set(year, month, dayOfMonth);

                    tvDateTo.setText(String.format("%d/%d/%d", month + 1, dayOfMonth, year));
                }
            }
        }, year, month, day);

        datePickerDialog.show();
    }
}