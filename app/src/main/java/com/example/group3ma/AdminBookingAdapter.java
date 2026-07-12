package com.example.group3ma;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private Context context;
    private DatabaseReference mDatabase;
    private UserSession session;
    private static final String TAG = "AdminBookingAdapter";

    public AdminBookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        this.session = new UserSession(context);
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvEmail.setText(booking.email);
        holder.tvHostel.setText(context.getString(R.string.hostel_room_format, booking.hostel, booking.roomType));
        holder.tvReceipt.setText(context.getString(R.string.receipt_format, (booking.receiptId != null ? booking.receiptId : context.getString(R.string.na))));
        holder.tvStatus.setText(context.getString(R.string.status_format, booking.status));

        updateStatusStyle(holder.tvStatus, booking.status, holder.btnApprove, holder.btnDecline);

        holder.btnApprove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            
            if (booking.receiptId != null && !TextUtils.isEmpty(booking.receiptId) && !booking.receiptId.equals("N/A")) {
                showConfirmationDialog(context.getString(R.string.approve_booking), 
                        context.getString(R.string.approve_confirmation, booking.email), () -> {
                    updateBookingStatus(booking, "Approved", currentPos);
                    sendApprovalSms(booking);
                });
            } else {
                Toast.makeText(context, "Cannot Approve: Missing Transaction Reference", Toast.LENGTH_LONG).show();
            }
        });
        
        holder.btnDecline.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            showConfirmationDialog(context.getString(R.string.decline_booking), 
                    context.getString(R.string.decline_confirmation), () -> updateBookingStatus(booking, "Declined", currentPos));
        });

        holder.btnDeleteBooking.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;
            showConfirmationDialog("Delete Booking", "Are you sure you want to PERMANENTLY delete this booking record?", () -> {
                deleteBooking(booking, currentPos);
            });
        });
    }

    private void deleteBooking(Booking booking, int position) {
        if (booking.bookingId != null) {
            mDatabase.child("bookings").child(booking.bookingId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    logAction("Admin DELETED booking record for " + booking.email);
                    Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show();
                    // Note: The Admin activity fetchBookings list will refresh automatically due to addValueEventListener
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void sendApprovalSms(Booking booking) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            String phoneNumber = booking.phone;
            if (phoneNumber != null && !TextUtils.isEmpty(phoneNumber)) {
                try {
                    String message = context.getString(R.string.approval_sms, booking.hostel, booking.receiptId);
                    SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(context, "Approval SMS sent to student", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "SMS Failed", e);
                }
            }
        }
    }

    private void updateStatusStyle(TextView tvStatus, String status, Button btnApprove, Button btnDecline) {
        btnApprove.setVisibility(View.VISIBLE);
        btnDecline.setVisibility(View.VISIBLE);

        if ("Approved".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_success));
            btnApprove.setVisibility(View.GONE);
        } else if ("Declined".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_error));
            btnDecline.setVisibility(View.GONE);
        } else if ("Paid".equalsIgnoreCase(status) || "Success".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primaryBlue));
        } else if ("Pending Payment".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accentAmber));
        } else {
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBookingStatus(Booking booking, String newStatus, int position) {
        if (booking.bookingId != null) {
            mDatabase.child("bookings").child(booking.bookingId).child("status").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        booking.status = newStatus;
                        notifyItemChanged(position);
                        logAction("Admin " + newStatus + " booking [ID: " + booking.bookingId + "] for " + booking.email);
                        Toast.makeText(context, "Booking " + newStatus + " successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void logAction(String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SystemLog log = new SystemLog(session.getEmail(), "Admin", timestamp, action);
        mDatabase.child("system_logs").push().setValue(log);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvHostel, tvReceipt, tvStatus;
        Button btnApprove, btnDecline, btnDeleteBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvBookingEmail);
            tvHostel = itemView.findViewById(R.id.tvBookingHostel);
            tvReceipt = itemView.findViewById(R.id.tvBookingReceipt);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnDeleteBooking = itemView.findViewById(R.id.btnDeleteBooking);
        }
    }
}
