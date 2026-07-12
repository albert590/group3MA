package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide ActionBar for a clean "normal app" look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_main);
        
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        
        // Ensure we are authenticated (Anonymously) before checking database
        signInAnonymouslyAndCheckSwitch();

        EdgeToEdge.enable(this);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, loginpage.class));
        });
        
        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Register...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, registerpage.class));
        });
    }

    private void signInAnonymouslyAndCheckSwitch() {
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Anonymous auth successful");
                    checkKillSwitch();
                } else {
                    Log.e(TAG, "Anonymous auth failed", task.getException());
                    checkKillSwitch(); // Try anyway
                }
            });
        } else {
            checkKillSwitch();
        }
    }

    private void checkKillSwitch() {
        mDatabase.child("app_settings").child("is_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    // App is deactivated
                    showDisabledScreen();
                } else {
                    // App is active, proceed with session management
                    proceedToApp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MAIN_ACTIVITY", "Kill switch check failed: " + error.getMessage());
                // Fallback: Proceed to app if database is unreachable or permission denied
                proceedToApp();
            }
        });
    }

    private void showDisabledScreen() {
        setContentView(R.layout.activity_app_disabled);
        TextView tvMessage = findViewById(R.id.tvDisabledMessage);
        if (tvMessage != null) {
            tvMessage.setText("This application has been temporarily deactivated by the administrator for security reasons.");
        }
        
        Button btnAdminLogin = findViewById(R.id.btnAdminLoginDisabled);
        if (btnAdminLogin != null) {
            btnAdminLogin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminLoginActivity.class));
            });
        }
    }

    private void proceedToApp() {
        UserSession session = new UserSession(this);
        
        // Security: Always logout when returning to MainActivity/Splash to force re-login
        // as per user requirement "once user left the app or account open should be closed"
        if (session.isLoggedIn()) {
            session.logoutUser();
        }
    }
}
