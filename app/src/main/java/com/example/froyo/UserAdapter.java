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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private ArrayList<User> arrayList;
    private Context context;
//    private long mNow;
//    private Date mDate;
//    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public UserAdapter(ArrayList<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }
    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user, parent, false);
        UserHolder holder = new UserHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).image)
                .into(holder.image);
        holder.userId.setText(arrayList.get(position).id);

        if(arrayList.get(position).isSelected){
            holder.selected.setBackgroundResource(R.drawable.selected_button_background);
        }
        else{
            holder.selected.setBackgroundResource(R.drawable.not_selected_button_background);
        }
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }
    public void clearData() {
        arrayList.clear();
        notifyDataSetChanged();
    }
    public class UserHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userId;
        android.widget.Button selected;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.image);
            this.userId = itemView.findViewById(R.id.userId);
            this.selected = itemView.findViewById(R.id.selected);
        }
    }
//    private String getTime(){
//        mNow = System.currentTimeMillis();
//        mDate = new Date(mNow);
//        return mFormat.format(mDate);
//    }
//    public String changeTimeFormat(String time){
//
//        Date currentDate = null; // your Date object
//        Date lastDate = null; // your another Date object
//
//        try {
//            currentDate = mFormat.parse(getTime());
//            lastDate = mFormat.parse(time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        if (currentDate != null && lastDate != null) {
//            long differenceInMillis = currentDate.getTime() - lastDate.getTime();
//            long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);
//
//            if (differenceInDays == 0) {
//                return "Today " + time.substring(11, 16);
//            } else if (differenceInDays == 1) {
//                return "Yesterday " + time.substring(11, 16);
//            } else {
//                String lastDateString = String.valueOf(lastDate);
//                return lastDateString.substring(4, 16);
//            }
//        }
//        else{
//            return "";
//        }
//    }
}

