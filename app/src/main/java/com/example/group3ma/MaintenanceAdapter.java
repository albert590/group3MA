package com.example.group3ma;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ClassEscapesDefinedScope")
public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {
    private final List<MaintenanceReport> reportList;

    public MaintenanceAdapter(List<MaintenanceReport> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceReport report = reportList.get(position);
        
        holder.tvCategory.setText(report.category);
        holder.tvStatus.setText(report.status);
        holder.tvDescription.setText(report.description);
        holder.tvHostel.setText(report.hostelId); // Currently storing name/id as string
        
        String date = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(new Date(report.timestamp));
        holder.tvDate.setText(date);

        // Optional: Color status badge
        if ("Resolved".equalsIgnoreCase(report.status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_background_resolved); // Assuming this exists or using a generic one
        } else if ("In Progress".equalsIgnoreCase(report.status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.badge_background_progress);
        }
    }

    @Override
    public int getItemCount() { return reportList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvStatus, tvDescription, tvHostel, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvReportCategory);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvDescription = itemView.findViewById(R.id.tvReportDescription);
            tvHostel = itemView.findViewById(R.id.tvReportHostel);
            tvDate = itemView.findViewById(R.id.tvReportDate);
        }
    }
}
