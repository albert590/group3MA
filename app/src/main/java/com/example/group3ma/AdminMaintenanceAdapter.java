package com.example.group3ma;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMaintenanceAdapter extends RecyclerView.Adapter<AdminMaintenanceAdapter.ViewHolder> {
    private List<MaintenanceReport> reportList;
    private DatabaseReference mDatabase;

    public AdminMaintenanceAdapter(List<MaintenanceReport> reportList) {
        this.reportList = reportList;
        this.mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference().child("maintenance_reports");
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
        holder.tvHostel.setText("Hostel: " + report.hostelId);
        
        String date = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(new Date(report.timestamp));
        holder.tvDate.setText(date);

        holder.itemView.setOnClickListener(v -> showStatusUpdateDialog(v, report));
    }

    private void showStatusUpdateDialog(View v, MaintenanceReport report) {
        String[] statuses = {"Pending", "In Progress", "Resolved"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Update Report Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    updateStatusInFirebase(report.reportId, newStatus, v);
                })
                .show();
    }

    private void updateStatusInFirebase(String reportId, String status, View v) {
        mDatabase.child(reportId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Status updated to " + status, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show());
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
