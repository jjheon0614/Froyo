package com.example.froyo;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentDialogFragment extends DialogFragment {

    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private EditText commentInput;
    private ImageButton postCommentButton;
    private String postId, currentUserEmail, currentUsername, currentUserImg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_dialog, container, false);

        // Retrieve postId from arguments
        Bundle args = getArguments();


        if (args != null) {
            postId = args.getString("postId", "");
            currentUserEmail = args.getString("userEmail", "");
        }


        fetchUserData();

        // Initialize RecyclerView and adapter
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add divider between comments
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        commentsRecyclerView.addItemDecoration(dividerItemDecoration);

        commentAdapter = new CommentAdapter(getCommentsDataFromFirestore(postId)); // Implement getCommentsData() to fetch your comments
        commentsRecyclerView.setAdapter(commentAdapter);

        // Set the title if needed
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Comments");


        commentInput = view.findViewById(R.id.commentInput);
        postCommentButton = view.findViewById(R.id.postCommentButton);
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommentToFirestore(postId, commentInput.getText().toString());
                commentInput.setText("");
                // Toast.makeText(getContext(), "Current User: " , Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private List<String> getCommentsData() {
        // Implement this method to fetch and return your comments data
        // For now, returning a dummy list
        List<String> comments = new ArrayList<>();
        comments.add("Comment 1");
        comments.add("Comment 2");
        comments.add("Comment 3");
        return comments;
    }


    private void postCommentToFirestore(String postId, String newComment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map for the new comment including user information
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentContent", newComment);
        commentMap.put("profileImage", currentUserImg);
        commentMap.put("user", currentUsername);

        // Assuming your comments are stored as a field in each post document
        db.collection("posts")
                .document(postId)
                .update("comments", FieldValue.arrayUnion(commentMap))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Comment posted successfully
                            // Fetch updated comments and update RecyclerView
                            getCommentsDataFromFirestore(postId);
                        } else {
                            // Handle failure to post comment
                            Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private List<Map<String, String>> getCommentsDataFromFirestore(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Map<String, String>> comments = new ArrayList<>();

        // Assuming your comments are stored as a field in each post document
        db.collection("posts")
                .document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve comments field from the post document
                                List<Map<String, String>> commentsFromFirestore = (List<Map<String, String>>) document.get("comments");

                                if (commentsFromFirestore != null) {
                                    // Update RecyclerView with fetched comments
                                    updateCommentsRecyclerView(commentsFromFirestore);
                                }
                            } else {
                                // Document does not exist
                            }
                        } else {
                            // Handle failure
                        }
                    }
                });
        return comments;
    }


    private void updateCommentsRecyclerView(List<Map<String, String>>comments) {
        commentAdapter.setComments(comments);
        commentAdapter.notifyDataSetChanged();
    }

    private void fetchUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming email is unique and only one document is returned
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            currentUserImg = documentSnapshot.getString("imageUrl");
                            currentUsername = documentSnapshot.getString("username");

                            // Log the retrieved data for debugging
                            Log.d("UserData", "User data retrieved successfully");
                            Log.d("UserData", "User Image URL: " + currentUserImg);
                            Log.d("UserData", "Username: " + currentUsername);

                            // Now, you have the username and imageUrl, you can use them as needed
                            // For example, you can pass them to another function or update UI
                        } else {
                            // Handle case where user data does not exist
                            Log.d("UserData", "No user data found for email: " + currentUserEmail);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("UserData", "Error fetching user data", e);
                    }
                });
    }
}
