package com.example.group3ma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class paymentstatus extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private TextView tvReceiptIdValue, tvStatusValue;
    private DatabaseReference mDatabase;
    private static final String TAG = "PaymentStatus";
    private UserSession session;
    private String bookingId;
    private ValueEventListener bookingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_paymentstatus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        session = new UserSession(this);
        tvReceiptIdValue = findViewById(R.id.tvReceiptIdValue);
        tvStatusValue = findViewById(R.id.tvStatusValue);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        Button btnSendReceiptEmail = findViewById(R.id.btnSendReceiptEmail);
        Button btnLogout = findViewById(R.id.btnLogout);
        
        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        bookingId = getIntent().getStringExtra("BOOKING_ID");
        String receiptId = getIntent().getStringExtra("RECEIPT_ID");
        String status = getIntent().getStringExtra("STATUS");

        if (receiptId != null) tvReceiptIdValue.setText(receiptId);
        if (status != null) updateStatusView(status);

        if (bookingId != null) {
            listenForBookingUpdates();
        } else {
            fetchLatestPaymentStatus();
        }

        btnBackToHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, HomeDashboardActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });

        btnSendReceiptEmail.setOnClickListener(v -> sendReceiptEmail(tvReceiptIdValue.getText().toString()));

        btnLogout.setOnClickListener(v -> {
            session.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, loginpage.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });
    }

    private void listenForBookingUpdates() {
        bookingListener = mDatabase.child("bookings").child(bookingId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    tvReceiptIdValue.setText(booking.receiptId != null ? booking.receiptId : "N/A");
                    updateStatusView(booking.status);
                    
                    if ("Approved".equalsIgnoreCase(booking.status) || "Paid".equalsIgnoreCase(booking.status)) {
                        checkAndSendSuccessSms(booking.receiptId);
                        logPaymentSuccess(booking.receiptId);
                        
                        // Auto-navigate to dashboard after success
                        Toast.makeText(paymentstatus.this, "Payment Confirmed! Redirecting...", Toast.LENGTH_LONG).show();
                        tvStatusValue.postDelayed(() -> {
                            if (!isFinishing()) {
                                Intent homeIntent = new Intent(paymentstatus.this, HomeDashboardActivity.class);
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(homeIntent);
                                finish();
                            }
                        }, 3000);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logPaymentSuccess(String receiptId) {
        String email = session.getEmail();
        if (email != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            mDatabase.child("system_logs").push().setValue(new SystemLog(email, "Student", timestamp, "Payment Confirmed: " + receiptId));
        }
    }

    private void updateStatusView(String status) {
        if (status != null) {
            tvStatusValue.setText(status);
            int colorRes = R.color.status_pending;
            if ("Approved".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(status) || "Success".equalsIgnoreCase(status)) {
                colorRes = R.color.status_success;
            } else if ("Declined".equalsIgnoreCase(status) || "Failed".equalsIgnoreCase(status)) {
                colorRes = R.color.status_error;
            }
            tvStatusValue.setTextColor(ContextCompat.getColor(this, colorRes));
        }
    }

    private void fetchLatestPaymentStatus() {
        String email = session.getEmail();
        if (email == null) return;

        mDatabase.child("bookings").orderByChild("email").equalTo(email).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Booking booking = child.getValue(Booking.class);
                        if (booking != null) {
                            bookingId = child.getKey();
                            listenForBookingUpdates();
                        }
                    }
                } else {
                    tvStatusValue.setText("No recent bookings");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkAndSendSuccessSms(String receiptId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSuccessSms(receiptId);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    private void sendSuccessSms(String receiptId) {
        String phoneNumber = session.getPhone();
        if (phoneNumber != null && !TextUtils.isEmpty(phoneNumber)) {
            try {
                String message = "MMUST Hostel: Your booking is " + tvStatusValue.getText().toString() + ". Ref: " + receiptId;
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception e) {
                Log.e(TAG, "SMS Failed", e);
            }
        }
    }

    private void sendReceiptEmail(String receiptId) {
        String email = session.getEmail();
        if (email == null) return;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hostel Booking Receipt");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Your booking status: " + tvStatusValue.getText().toString() + "\nReceipt ID: " + receiptId);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Receipt"));
        } catch (Exception ex) {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingId != null && bookingListener != null) {
            mDatabase.child("bookings").child(bookingId).removeEventListener(bookingListener);
        }
    }
}
