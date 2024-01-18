package com.example.froyo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PostListViewAdapter extends RecyclerView.Adapter<PostListViewAdapter.ViewHolder> {

    private ArrayList<Post> posts = new ArrayList<>();

    private Context context;
    public PostListViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArrayList<String> postHashtags = posts.get(position).getHashTag();

        // Check if postHashtags is null or empty
        if (postHashtags == null || postHashtags.isEmpty()) {
            // If null or empty, create a list containing a single empty string
            postHashtags = new ArrayList<>();
            postHashtags.add("");
        } else {
            // Convert the list of hashtags into a formatted string
            StringBuilder hashtagsStringBuilder = new StringBuilder();
            for (String hashtag : postHashtags) {
                hashtagsStringBuilder.append("#").append(hashtag).append(" ");
            }
            holder.hashtags.setText(hashtagsStringBuilder.toString());
        }

        holder.majorTag.setText(posts.get(position).getMajorTag());
        //holder.profileName.setText(posts.get(position).getId());
        //holder.profileName.setText(posts.get(position).getId());
        holder.profileName.setText(posts.get(position).getUserEmail());
        holder.postContent.setText(posts.get(position).getContent());
        ArrayList<String> images = posts.get(position).getImages();
        Glide.with(context).asBitmap().load(images.get(0)).into(holder.postImage);
        holder.likeCount.setText(String.valueOf(posts.get(position).getLikes()));
        holder.commentCount.setText(String.valueOf(posts.get(position).getComments().size()));


        holder.postsParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, posts.get(holder.getAdapterPosition()).getId() , Toast.LENGTH_SHORT).show();
            }
        });

        // Set the OnClickListener for the profileName TextView
        holder.profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent = new Intent(context, ProfileInfoActivity.class);
                // Put extra data if needed, e.g., the user email or ID
                intent.putExtra("userEmail", posts.get(position).getUserEmail());
                if (user != null) {
                    String email = user.getEmail();
                    intent.putExtra("email", email);
                }
                // You can add more data to intent if required

                context.startActivity(intent);
            }
        });

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLikeButtonClick(position);
                Post currentPost = posts.get(position);
                holder.likeCount.setText(String.valueOf(currentPost.getLikes()));
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the comment dialog fragment
                showCommentDialogFragment(posts.get(holder.getAdapterPosition()).getId());
            }
        });




    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void showCommentDialogFragment(String postId) {
        CommentDialogFragment commentDialogFragment = new CommentDialogFragment();

        // Pass the postId to the CommentDialogFragment using arguments
        Bundle args = new Bundle();
        args.putString("postId", postId);
        commentDialogFragment.setArguments(args);

        // Use the FragmentManager from the context (activity)
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        commentDialogFragment.show(fragmentManager, CommentDialogFragment.class.getSimpleName());
    }


    private void handleLikeButtonClick(int position) {
        Post currentPost = posts.get(position);

        // Increment likes count
        currentPost.setLikes(currentPost.getLikes() + 1);

        // Update UI
        // updateLikeUI(position);

        // Update likes count in Firestore
        updateLikesInFirestore(currentPost.getId(), currentPost.getLikes());
    }

    private void updateLikesInFirestore(String postId, int likesCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Assuming you have a "posts" collection in Firestore
        db.collection("posts").document(postId)
                .update("likes", likesCount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully updated likes count in Firestore
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to update likes count in Firestore
                    }
                });
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView profileName, postContent, likeCount, commentCount, hashtags, majorTag;
        // private ImageView profileImage;
        private ImageView postImage, likeButton, commentButton;
        private CardView postsParent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profileName);
            postContent = itemView.findViewById(R.id.postContent);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            // profilpostContenteImage = itemView.findViewById(R.id.profileImage);
            postImage = itemView.findViewById((R.id.postImage));
            postsParent = itemView.findViewById(R.id.postsParent);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            hashtags = itemView.findViewById(R.id.hashtags);
            majorTag = itemView.findViewById(R.id.majorTag);
        }
    }
}
