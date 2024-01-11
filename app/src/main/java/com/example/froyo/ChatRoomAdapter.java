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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomHolder> {

    private ArrayList<ChatRoom> arrayList;
    private Context context;
    private long mNow;
    private Date mDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public ChatRoomAdapter(ArrayList<ChatRoom> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatRoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room, parent, false);
        ChatRoomHolder holder = new ChatRoomHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).profile)
                .into(holder.profile);
        holder.userId.setText(arrayList.get(position).title);
        holder.currentMessage.setText(arrayList.get(position).currentMessage);
        if(arrayList.get(position).nonCheckedMessage > 0){
            holder.nonChecked.setText(String.valueOf(arrayList.get(position).nonCheckedMessage));
            Toast.makeText(context, String.valueOf(arrayList.get(position).nonCheckedMessage),
                    Toast.LENGTH_LONG).toString();
            holder.row.setBackgroundResource(R.drawable.chat_list_row);
        }
        else{
            holder.nonChecked.setVisibility(View.GONE);
        }
        holder.time.setText(changeTimeFormat(arrayList.get(position).time));
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }
    public void clearData() {
        arrayList.clear();
        notifyDataSetChanged();
    }
    public class ChatRoomHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView userId;
        TextView currentMessage;
        TextView nonChecked;
        TextView time;
        LinearLayout row;
        public ChatRoomHolder(@NonNull View itemView) {
            super(itemView);
            this.profile = itemView.findViewById(R.id.profile);
            this.userId = itemView.findViewById(R.id.userId);
            this.currentMessage = itemView.findViewById(R.id.currentMessage);
            this.nonChecked = itemView.findViewById(R.id.nonChecked);
            this.time = itemView.findViewById(R.id.time);
            this.row = itemView.findViewById(R.id.row);
        }
    }
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
    public String changeTimeFormat(String time){

        Date currentDate = null; // your Date object
        Date lastDate = null; // your another Date object

        try {
            currentDate = mFormat.parse(getTime());
            lastDate = mFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (currentDate != null && lastDate != null) {
            long differenceInMillis = currentDate.getTime() - lastDate.getTime();
            long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);

            if (differenceInDays == 0) {
                return "Today " + time.substring(11, 16);
            } else if (differenceInDays == 1) {
                return "Yesterday " + time.substring(11, 16);
            } else {
                String lastDateString = String.valueOf(lastDate);
                return lastDateString.substring(4, 16);
            }
        }
        else{
            return "";
        }
    }
}
