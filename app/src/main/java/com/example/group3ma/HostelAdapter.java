package com.example.group3ma;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HostelAdapter extends RecyclerView.Adapter<HostelAdapter.HostelViewHolder> {

    private List<Hostel> hostelList;

    public HostelAdapter(List<Hostel> hostelList) {
        this.hostelList = hostelList;
    }

    @NonNull
    @Override
    public HostelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hostel, parent, false);
        return new HostelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostelViewHolder holder, int position) {
        Hostel hostel = hostelList.get(position);
        holder.tvHostelName.setText(hostel.getName());
        holder.tvCapacity.setText("Capacity Remaining: " + hostel.getCapacity());
        holder.tvPrice.setText(hostel.getPrice());
        holder.ivHostelImage.setImageResource(hostel.getImageResId());
        
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), HostelDetailsActivity.class);
            // In a real app, you would pass data here using intent.putExtra()
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hostelList.size();
    }

    static class HostelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHostelImage;
        TextView tvHostelName;
        TextView tvCapacity;
        TextView tvPrice;
        Button btnViewDetails;

        public HostelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHostelImage = itemView.findViewById(R.id.ivHostelImage);
            tvHostelName = itemView.findViewById(R.id.tvHostelName);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}