package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class bookingform extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String registeredEmail;
    private static final String TAG = "BookingForm";
    private UserSession session;
    private boolean isSystemActive = true;

    // MPESA CONSTANTS (SANDBOX)
    private static final String CONSUMER_KEY = "wbHfZJz9sGVnxMTTTU457AOlaOyW4gJGwD6ADvPzfFqOHvVo";
    private static final String CONSUMER_SECRET = "PjbnAbJwzbG8D7H6lVwNJRo7HnAjt40aVMzs58pUo71hmHeGDHQzfBLkl6ccESZf";
    
    // ADMIN MPESA DETAILS
    private static final String ADMIN_BUSINESS_SHORT_CODE = "174379"; 
    private static final String ADMIN_PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    private static final String CALLBACK_URL = "https://mydomain.com/path";
    private static final String ADMIN_NAME = "ALBERT NAMASAKA WEKESA";

    private Button btnPayWithMpesa;
    private Button btnSubmitBooking;
    private ImageView ivHeaderImage;
    private TextView tvDynamicPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookingform);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left, 0, 0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        session = new UserSession(this);
        registeredEmail = session.getEmail();

        checkSystemStatus();

        ivHeaderImage = findViewById(R.id.ivHeaderImage);
        EditText etHostelName = findViewById(R.id.etHostelName);
        RadioGroup rgRoomType = findViewById(R.id.rgRoomType);
        EditText etMpesaPhone = findViewById(R.id.etMpesaPhone);
        TextView tvAdminName = findViewById(R.id.tvAdminName);
        tvDynamicPrice = findViewById(R.id.tvDynamicPrice);
        btnSubmitBooking = findViewById(R.id.btnSubmitBooking);
        btnPayWithMpesa = findViewById(R.id.btnPayWithMpesa);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Update price display when selection changes
        rgRoomType.setOnCheckedChangeListener((group, checkedId) -> {
            String price = "5000";
            if (checkedId == R.id.rbSingle) {
                price = "10000";
            }
            if (tvDynamicPrice != null) {
                tvDynamicPrice.setText("Booking Price: KSH " + price);
            }
        });
        
        // Default to Shared
        rgRoomType.check(R.id.rbShared);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                session.logoutUser();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(bookingform.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (tvAdminName != null) tvAdminName.setText(ADMIN_NAME);

        String hostelName = getIntent().getStringExtra("HOSTEL_NAME");
        String hostelPrice = getIntent().getStringExtra("HOSTEL_PRICE");
        int hostelImage = R.drawable.zzz;

        if (hostelName != null) etHostelName.setText(hostelName);
        
        Glide.with(this)
             .load(hostelImage)
             .placeholder(R.drawable.zzz)
             .into(ivHeaderImage);

        if (session.getPhone() != null) etMpesaPhone.setText(session.getPhone());

        btnSubmitBooking.setOnClickListener(v -> {
            if (!isSystemActive) {
                Toast.makeText(this, "System is currently inactive", Toast.LENGTH_LONG).show();
                return;
            }
            checkExistingBooking(() -> {
                String mpesaPhone = etMpesaPhone.getText().toString().trim();
                if (mpesaPhone.isEmpty()) {
                    Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                int selectedId = rgRoomType.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Please select a room type", Toast.LENGTH_SHORT).show();
                    return;
                }
                String roomType = ((RadioButton) findViewById(selectedId)).getText().toString();
                saveBookingToFirebase(mpesaPhone, hostelName, roomType, "MANUAL", "Pending Approval");
            });
        });

        btnPayWithMpesa.setOnClickListener(v -> {
            if (!isSystemActive) {
                Toast.makeText(this, "System is currently inactive", Toast.LENGTH_LONG).show();
                return;
            }
            int selectedId = rgRoomType.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a room type before paying", Toast.LENGTH_SHORT).show();
                return;
            }
            checkExistingBooking(() -> initiatePayment(hostelPrice != null ? hostelPrice : "1", hostelName, rgRoomType, etMpesaPhone));
        });
    }

    private void checkSystemStatus() {
        mDatabase.child("app_settings").child("is_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean active = snapshot.getValue(Boolean.class);
                isSystemActive = active != null && active;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkExistingBooking(Runnable onSuccess) {
        if (registeredEmail == null) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, loginpage.class));
            finish();
            return;
        }
        mDatabase.child("bookings").orderByChild("email").equalTo(registeredEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasActiveBooking = false;
                String activeBookingId = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Booking b = child.getValue(Booking.class);
                    if (b != null && b.status != null && (b.status.equals("Approved") || b.status.equals("Pending Approval") || b.status.equals("Pending Payment") || b.status.equals("Paid"))) {
                        hasActiveBooking = true;
                        activeBookingId = child.getKey();
                        break;
                    }
                }
                if (hasActiveBooking) {
                    Toast.makeText(bookingform.this, "You already have an active booking or payment in progress. Redirecting...", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(bookingform.this, paymentstatus.class);
                    intent.putExtra("BOOKING_ID", activeBookingId);
                    startActivity(intent);
                    finish();
                } else {
                    onSuccess.run();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initiatePayment(String price, String hostelName, RadioGroup rgRoomType, EditText etMpesaPhone) {
        String mpesaPhone = etMpesaPhone.getText().toString().trim();
        if (mpesaPhone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String formattedPhone = "254" + (mpesaPhone.startsWith("0") ? mpesaPhone.substring(1) : mpesaPhone);
        int selectedId = rgRoomType.getCheckedRadioButtonId();
        String roomType = "Shared";
        String actualPrice = "5000"; // Default affordable price

        if (selectedId != -1) {
            RadioButton rb = findViewById(selectedId);
            roomType = rb.getText().toString();
            if (roomType.equalsIgnoreCase("Single")) {
                actualPrice = "10000";
            } else {
                actualPrice = "5000";
            }
        }

        btnPayWithMpesa.setEnabled(false);
        btnPayWithMpesa.setText("Processing KSH " + actualPrice + "...");
        performMpesaStkPush(formattedPhone, actualPrice, hostelName, roomType);
    }

    private void performMpesaStkPush(String phoneNumber, String amount, String hostelName, String roomType) {
        MpesaApiService apiService = ApiClient.getClient().create(MpesaApiService.class);
        String authString = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String authHeader = "Basic " + Base64.encodeToString(authString.getBytes(StandardCharsets.ISO_8859_1), Base64.NO_WRAP);
        
        // Ensure name is clean for payment reference
        String cleanHostelName = hostelName != null ? hostelName.replace("MMUST ", "").replace("MMUST", "").trim() : "Hostel";

        apiService.getAccessToken(authHeader).enqueue(new Callback<AccessTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccessTokenResponse> call, @NonNull Response<AccessTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    initiateStkPushWithToken(response.body().accessToken, phoneNumber, amount, cleanHostelName, roomType, apiService);
                } else {
                    resetPayButton();
                    Toast.makeText(bookingform.this, "Payment Auth Failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<AccessTokenResponse> call, @NonNull Throwable t) {
                resetPayButton();
                Toast.makeText(bookingform.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiateStkPushWithToken(String accessToken, String phoneNumber, String amount, String hostelName, String roomType, MpesaApiService apiService) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String password = Base64.encodeToString((ADMIN_BUSINESS_SHORT_CODE + ADMIN_PASSKEY + timestamp).getBytes(), Base64.NO_WRAP);
        
        String cleanAmount = amount.replaceAll("[^\\d]", "");
        if (cleanAmount.isEmpty()) cleanAmount = "1";
        int intAmount = Integer.parseInt(cleanAmount);

        PaymentRequest paymentRequest = new PaymentRequest(ADMIN_BUSINESS_SHORT_CODE, password, timestamp, "CustomerPayBillOnline", intAmount, phoneNumber, ADMIN_BUSINESS_SHORT_CODE, phoneNumber, CALLBACK_URL, "Booking", "Hostel " + hostelName);

        apiService.initiateStkPush("Bearer " + accessToken, paymentRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentResponse> call, @NonNull Response<PaymentResponse> response) {
                resetPayButton();
                if (response.isSuccessful() && response.body() != null) {
                    saveBookingToFirebase(phoneNumber, hostelName, roomType, response.body().getCheckoutRequestID(), "Pending Payment");
                } else {
                    Toast.makeText(bookingform.this, "STK Push Failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                resetPayButton();
                Toast.makeText(bookingform.this, "Transaction Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPayButton() {
        btnPayWithMpesa.setEnabled(true);
        btnPayWithMpesa.setText("Pay with MPESA");
    }

    private void saveBookingToFirebase(String mpesaPhone, String hostel, String roomType, String receiptId, String status) {
        String bookingId = mDatabase.child("bookings").push().getKey();
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("bookingId", bookingId);
        bookingData.put("email", registeredEmail);
        bookingData.put("phone", mpesaPhone);
        bookingData.put("hostel", hostel);
        bookingData.put("roomType", roomType);
        bookingData.put("receiptId", receiptId);
        bookingData.put("status", status);
        bookingData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        if (bookingId != null) {
            mDatabase.child("bookings").child(bookingId).setValue(bookingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (status.equals("Paid") || status.equals("Approved")) {
                        updateHostelAvailability(hostel);
                    }
                    logAction("Booking Request: " + hostel + " | Status: " + status + " | Ref: " + receiptId);
                    Intent intent = new Intent(this, paymentstatus.class);
                    intent.putExtra("BOOKING_ID", bookingId); // Added bookingId
                    intent.putExtra("RECEIPT_ID", receiptId);
                    intent.putExtra("STATUS", status);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Database error during booking", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateHostelAvailability(String hostelName) {
        mDatabase.child("hostels").orderByChild("name").equalTo(hostelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Hostel h = ds.getValue(Hostel.class);
                    if (h != null && h.getAvailableBeds() > 0) {
                        ds.getRef().child("availableBeds").setValue(h.getAvailableBeds() - 1);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logAction(String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SystemLog log = new SystemLog(registeredEmail, "Student", timestamp, action);
        mDatabase.child("system_logs").push().setValue(log);
    }
}
