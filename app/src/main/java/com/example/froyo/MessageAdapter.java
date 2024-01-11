package com.example.froyo;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder>{
    private ArrayList<Message> arrayList;
    private Context context;

    public MessageAdapter(ArrayList<Message> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box, parent, false);
        MessageAdapter.MessageHolder holder = new MessageAdapter.MessageHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageHolder holder, int position) {
        if(!arrayList.get(position).isMine){
            holder.userId.setText(arrayList.get(position).id);

            if(arrayList.get(position).message.contains("https://firebasestorage.googleapis.com/v0/b/android-chat-1de43.appspot.com/o/")){
                holder.message.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                holder.box.setBackgroundResource(R.color.chatRoomColor);
                DrawableImageViewTarget gifImage1 = new DrawableImageViewTarget(holder.image);
                Glide.with(context).load(arrayList.get(position).message).into(gifImage1);
            }
            else{
                holder.message.setText(arrayList.get(position).message);
                if(arrayList.get(position).isSearched){
                    //high light
                    int startIndex = -1;
                    if (arrayList.get(position).message != null && arrayList.get(position).searchStr != null && arrayList.get(position).message.length() >= arrayList.get(position).searchStr.length()) {
                        startIndex = arrayList.get(position).message.toLowerCase().indexOf(arrayList.get(position).searchStr.toLowerCase());
                        int endIndex = startIndex + arrayList.get(position).searchStr.length();

                        SpannableString spannableString = new SpannableString(arrayList.get(position).message);
                        spannableString.setSpan(new BackgroundColorSpan(Color.RED), startIndex, endIndex, 0);
                        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), startIndex, endIndex, 0);

                        holder.message.setText(spannableString);
                    }
                }
            }

            holder.time.setText(arrayList.get(position).time);
        }
        else{
            holder.userId.setText(arrayList.get(position).id);
            holder.userId.setTextColor(ContextCompat.getColor(context, R.color.black));

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.box.getLayoutParams();
            layoutParams.setMargins(240, 20, 0, 20);

            holder.box.setLayoutParams(layoutParams);

            if(arrayList.get(position).message.contains("https://firebasestorage.googleapis.com/v0/b/android-chat-1de43.appspot.com/o/")){
                holder.message.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                holder.box.setBackgroundResource(R.color.chatRoomColor);
                DrawableImageViewTarget gifImage1 = new DrawableImageViewTarget(holder.image);
                Glide.with(context).load(arrayList.get(position).message).into(gifImage1);
            }
            else{
                holder.box.setBackgroundResource(R.drawable.message_my_background);
                holder.message.setText(arrayList.get(position).message);
                holder.message.setTextColor(ContextCompat.getColor(context, R.color.white));
                if(arrayList.get(position).isSearched){
                    //high light
                    int startIndex = -1;
                    if (arrayList.get(position).message != null && arrayList.get(position).searchStr != null && arrayList.get(position).message.length() >= arrayList.get(position).searchStr.length()) {
                        startIndex = arrayList.get(position).message.toLowerCase().indexOf(arrayList.get(position).searchStr.toLowerCase());
                        int endIndex = startIndex + arrayList.get(position).searchStr.length();

                        SpannableString spannableString = new SpannableString(arrayList.get(position).message);
                        spannableString.setSpan(new BackgroundColorSpan(Color.RED), startIndex, endIndex, 0);

                        holder.message.setText(spannableString);
                    }
                }
            }

            holder.time.setText(arrayList.get(position).time);
        }
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }
    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView userId;
        TextView message;
        TextView time;
        ImageView image;
        LinearLayout box;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            this.userId = itemView.findViewById(R.id.userId);
            this.message = itemView.findViewById(R.id.message);
            this.image = itemView.findViewById(R.id.image);
            this.time = itemView.findViewById(R.id.time);
            this.box = itemView.findViewById(R.id.box);
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

