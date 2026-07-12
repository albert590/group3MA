package com.example.group3ma;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HostelRoommateAdapter extends RecyclerView.Adapter<HostelRoommateAdapter.ViewHolder> {
    private List<User> roommates;

    public HostelRoommateAdapter(List<User> roommates) {
        this.roommates = roommates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roommate_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = roommates.get(position);
        holder.tvName.setText(user.fullName);
        holder.tvYear.setText(user.yearOfStudy);

        holder.btnConnect.setOnClickListener(v -> {
            try {
                String phone = user.phoneNumber;
                if (phone != null && !phone.isEmpty()) {
                    String url = "https://api.whatsapp.com/send?phone=" + (phone.startsWith("0") ? "254" + phone.substring(1) : phone);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    v.getContext().startActivity(i);
                } else {
                    Toast.makeText(v.getContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return roommates.size();
    }

    public void updateList(List<User> newList) {
        this.roommates = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvYear;
        Button btnConnect;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoommateName);
            tvYear = itemView.findViewById(R.id.tvRoommateYear);
            btnConnect = itemView.findViewById(R.id.btnConnectRoommate);
        }
    }
}
