package com.example.froyo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ViewHolder> {

    private ArrayList<ListUser> userList;
    private ArrayList<ListUser> userListFull;
    private Context context;

    public ListUserAdapter(Context context, ArrayList<ListUser> userList) {
        this.context = context;
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public ListUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListUserAdapter.ViewHolder holder, int position) {
        ListUser user = userList.get(position);
        holder.userName.setText(user.getUsername());
        holder.userEmail = user.getEmail();

        // Use Glide to load the image
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getImageUrl())
                    .into(holder.userImage);
        }

        // Make delete button visible only for admin user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && "admin@gmail.com".equals(currentUser.getEmail())) {
            holder.deleteUserBtn.setVisibility(View.VISIBLE);
            holder.deleteUserBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the delete user action

                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Froyo);
                    builder.setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this user and all their posts?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // If yes, proceed with deletion
                                    Toast.makeText(context, "yes", Toast.LENGTH_SHORT).show();
//                                    deleteUserAndPosts(holder.userEmail);
                                }
                            })
                            .setNegativeButton("No", null).show();

                }
            });
        } else {
            holder.deleteUserBtn.setVisibility(View.GONE);
        }
    }

    private void deleteUserAndPosts(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, get the user document
        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot userSnapshot : queryDocumentSnapshots) {
                            if (userSnapshot.contains("postsArr")) {
                                List<Map<String, Object>> postsArr = (List<Map<String, Object>>) userSnapshot.get("postsArr");
                                if (postsArr != null) {
                                    // Iterate over the list of maps
                                    for (Map<String, Object> postEntry : postsArr) {
                                        // Get postId from each map entry
                                        String postId = (String) postEntry.get("postId");
                                        if (postId != null) {
                                            db.collection("posts").document(postId).delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Handle successful deletion of each post
                                                        Log.d("DeletePost", "Post deleted successfully: " + postId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Handle failure to delete each post
                                                        Log.e("DeletePost", "Error deleting post: " + postId, e);
                                                    });
                                        }
                                    }
                                }
                            }

                            // After all posts are deleted, delete the user
                            db.collection("users").document(userSnapshot.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "User and posts deleted successfully!", Toast.LENGTH_SHORT).show();
                                        // Update UI or notify adapter if necessary
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error deleting user.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error finding user.", Toast.LENGTH_SHORT).show();
                });
    }

    public void filter(String searchTerm) {
        userList.clear();
        if (searchTerm.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String filterPattern = searchTerm.toLowerCase().trim();
            for (ListUser user : userListFull) {
                if (user.getUsername().toLowerCase().contains(filterPattern)) {
                    userList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImage;
        public TextView userName;
        public String userEmail;
        public Button deleteUserBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            deleteUserBtn = itemView.findViewById(R.id.deleteUserBtn);

            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Intent intent = new Intent(context, ProfileInfoActivity.class);
                    // Put extra data if needed, e.g., the user email or ID
                    intent.putExtra("userEmail", userEmail);
                    if (user != null) {
                        String email = user.getEmail();
                        intent.putExtra("email", email);
                    }
                    // You can add more data to intent if required
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent);
                }
            });
        }
    }
}

