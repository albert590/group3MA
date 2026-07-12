package com.example.group3ma;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class HostelAdapter extends RecyclerView.Adapter<HostelAdapter.HostelViewHolder> {

    private List<Hostel> hostelList;
    private List<Hostel> hostelListFull;
    private Context context;

    public HostelAdapter(List<Hostel> hostelList) {
        this.hostelList = hostelList;
        this.hostelListFull = new ArrayList<>(hostelList);
    }

    @NonNull
    @Override
    public HostelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_hostel, parent, false);
        return new HostelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostelViewHolder holder, int position) {
        Hostel hostel = hostelList.get(position);

        // Remove "MMUST" from display name if it exists in database data
        String cleanName = hostel.getName() != null ? hostel.getName().replace("MMUST ", "").replace("MMUST", "").trim() : "Hostel";
        holder.tvHostelName.setText(cleanName);
        
        holder.tvCapacity.setText("Capacity: " + hostel.getCapacity());
        
        // Ensure price display is affordable and clear
        String price = hostel.getPrice();
        if (price != null && price.contains("15,000")) {
            price = "Shared: 5,000 | Single: 10,000";
        }
        holder.tvPrice.setText(price);

        // Handle Verified Badge
        if (hostel.isVerified()) {
            holder.tvVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvVerifiedBadge.setVisibility(View.GONE);
        }

        // Set zzz image for all hostels as requested
        Glide.with(context)
             .load(R.drawable.zzz)
             .placeholder(R.drawable.img)
             .into(holder.ivHostelImage);

        holder.btnViewDetails.setOnClickListener(v -> {
            // Using HostelDetailsActivityImpl to get the enhanced features
            Intent intent = new Intent(v.getContext(), HostelDetailsActivityImpl.class);
            intent.putExtra("HOSTEL_NAME", hostel.getName());
            intent.putExtra("HOSTEL_ID", hostel.getId());
            // Add flag if context is not an activity
            if (!(v.getContext() instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hostelList.size();
    }

    public void updateList(List<Hostel> newList) {
        this.hostelList = new ArrayList<>(newList);
        this.hostelListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        applyFilters(text, 0, Float.MAX_VALUE, null, false);
    }

    public void applyFilters(String query, float minPrice, float maxPrice, List<String> requiredAmenities, boolean verifiedOnly) {
        hostelList.clear();
        for (Hostel hostel : hostelListFull) {
            boolean matches = true;

            // Search query
            if (query != null && !query.isEmpty()) {
                if (!hostel.getName().toLowerCase().contains(query.toLowerCase())) {
                    matches = false;
                }
            }

            // Price filtering
            if (matches) {
                try {
                    // Extract number from "KSH 15,000 / Semester"
                    String priceStr = hostel.getPrice().replaceAll("[^0-9]", "");
                    if (!priceStr.isEmpty()) {
                        float price = Float.parseFloat(priceStr);
                        if (price < minPrice || price > maxPrice) {
                            matches = false;
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Amenities filtering
            if (matches && requiredAmenities != null && !requiredAmenities.isEmpty()) {
                List<String> hostelAmenities = hostel.getAmenities();
                if (hostelAmenities == null) {
                    matches = false;
                } else {
                    for (String req : requiredAmenities) {
                        if (!hostelAmenities.contains(req)) {
                            matches = false;
                            break;
                        }
                    }
                }
            }

            // Verified filtering
            if (matches && verifiedOnly) {
                if (!hostel.isVerified()) {
                    matches = false;
                }
            }

            if (matches) {
                hostelList.add(hostel);
            }
        }
        notifyDataSetChanged();
    }

    static class HostelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHostelImage;
        TextView tvHostelName, tvCapacity, tvPrice, tvVerifiedBadge;
        Button btnViewDetails;

        public HostelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHostelImage = itemView.findViewById(R.id.ivHostelImage);
            tvHostelName = itemView.findViewById(R.id.tvHostelName);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
