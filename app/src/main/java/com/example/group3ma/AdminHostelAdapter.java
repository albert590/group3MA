package com.example.group3ma;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminHostelAdapter extends RecyclerView.Adapter<AdminHostelAdapter.ViewHolder> {

    private List<Hostel> hostelList;
    private OnHostelActionListener listener;

    public interface OnHostelActionListener {
        void onEdit(Hostel hostel);
        void onDelete(Hostel hostel);
    }

    public AdminHostelAdapter(List<Hostel> hostelList, OnHostelActionListener listener) {
        this.hostelList = hostelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_hostel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hostel hostel = hostelList.get(position);
        
        // Remove "MMUST" from display name
        String cleanName = hostel.getName() != null ? hostel.getName().replace("MMUST ", "").replace("MMUST", "").trim() : "Hostel";
        holder.tvName.setText(cleanName);

        // Ensure price display is affordable
        String price = hostel.getPrice();
        if (price != null && price.contains("15,000")) {
            price = "Shared: 5,000 | Single: 10,000";
        }
        
        holder.tvDetails.setText("Capacity: " + hostel.getCapacity() + " | " + price);
        
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(hostel));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(hostel));
    }

    @Override
    public int getItemCount() {
        return hostelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        Button btnEdit, btnDelete;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvAdminHostelName);
            tvDetails = view.findViewById(R.id.tvAdminHostelDetails);
            btnEdit = view.findViewById(R.id.btnEditHostel);
            btnDelete = view.findViewById(R.id.btnDeleteHostel);
        }
    }
}
