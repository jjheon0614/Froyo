package com.example.froyo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        // String NameString = foodPlaces.get(position).getName() + " - " + foodPlaces.get(position).getCategory();

        holder.profileName.setText(posts.get(position).getId());
        holder.postContent.setText(posts.get(position).getContent());
        ArrayList<String> images = posts.get(position).getImages();
        Glide.with(context).asBitmap().load(images.get(0)).into(holder.postImage);


        holder.postsParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, posts.get(holder.getAdapterPosition()).getContent() , Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView profileName, postContent;
        // private ImageView profileImage;
        private ImageView postImage;
        private CardView postsParent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profileName);
            postContent = itemView.findViewById(R.id.postContent);
            // profilpostContenteImage = itemView.findViewById(R.id.profileImage);
            postImage = itemView.findViewById((R.id.postImage));
            postsParent = itemView.findViewById(R.id.postsParent);
        }
    }
}
