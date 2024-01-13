package com.example.froyo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class CommentDialogFragment extends DialogFragment {

    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private String postId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_dialog, container, false);

        // Retrieve postId from arguments
        Bundle args = getArguments();

        if (args != null) {
            postId = args.getString("postId", "");
        }

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

    private List<String> getCommentsDataFromFirestore(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> comments = new ArrayList<>();

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
                                List<String> comments = (List<String>) document.get("comments");

                                if (comments != null) {
                                    // Update RecyclerView with fetched comments
                                    updateCommentsRecyclerView(comments);
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

    private void updateCommentsRecyclerView(List<String> comments) {
        commentAdapter.setComments(comments);
        commentAdapter.notifyDataSetChanged();
    }

}
