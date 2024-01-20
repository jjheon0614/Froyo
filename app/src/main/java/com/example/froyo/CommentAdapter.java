package com.example.froyo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Map<String, String>> comments;

    public CommentAdapter(List<Map<String, String>> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // String comment = comments.get(position);
        Map<String, String> commentMap = comments.get(position);
        String username = commentMap.get("user");
        String commentContent = commentMap.get("commentContent");
        String profileImage = commentMap.containsKey("profileImage") ? commentMap.get("profileImage") : null;

        // Display username and comment content
        holder.commentText.setText(username + ": " + commentContent);

        // Load and display profile image if available
        if (profileImage != null) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImage)
                    .into(holder.profileImage);
        } else {
            // Handle the case where there is no profile image
            // You might want to hide the ImageView or set a default image
            holder.profileImage.setVisibility(View.GONE);
        }

        holder.commentText.setText(commentContent);
        holder.usernameText.setText(username);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Map<String, String>> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView commentText, usernameText;
        private ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.commentText);
            usernameText = itemView.findViewById(R.id.usernameText);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
