package com.example.froyo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

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

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);

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

