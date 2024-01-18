package com.example.froyo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MiniProfileAdapter extends RecyclerView.Adapter<MiniProfileAdapter.MiniProfileHolder> {

    private ArrayList<User> arrayList;
    private Context context;

    public MiniProfileAdapter(ArrayList<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }
    @NonNull
    @Override
    public MiniProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_profile, parent, false);
        MiniProfileHolder holder = new MiniProfileHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MiniProfileHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).image)
                .into(holder.image);
        holder.userId.setText(arrayList.get(position).id);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }
    public void clearData() {
        arrayList.clear();
        notifyDataSetChanged();
    }
    public class MiniProfileHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userId;
        public MiniProfileHolder(@NonNull View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.image);
            this.userId = itemView.findViewById(R.id.userId);
        }
    }
}


