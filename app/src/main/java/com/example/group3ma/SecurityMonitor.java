package com.example.group3ma;

import android.content.Context;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecurityMonitor {
    private static final String TAG = "SecurityMonitor";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static int failedLoginCount = 0;
    private static final String ADMIN_NOTIFICATION_EMAIL = "albertnamasaka73@gmail.com";

    /**
     * Records a failed login attempt. If attempts exceed threshold, triggers system lockdown.
     * @param context Application context
     * @param identifier The email or username that failed
     * @param userType "student" or "admin"
     */
    public static void recordFailedAttempt(Context context, String identifier, String userType) {
        failedLoginCount++;
        Log.w(TAG, "Failed login attempt #" + failedLoginCount + " for " + identifier);

        if (failedLoginCount >= MAX_FAILED_ATTEMPTS) {
            triggerSystemLockdown(context, identifier, userType);
        }
    }

    /**
     * Resets the failed attempt counter upon successful login.
     */
    public static void resetAttempts() {
        failedLoginCount = 0;
    }

    /**
     * Triggers the kill switch, logs the breach, and notifies the administrator.
     */
    public static void triggerSystemLockdown(Context context, String identifier, String userType) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        
        // 1. Activate Kill Switch in Firebase
        mDatabase.child("app_settings").child("is_active").setValue(false);

        // 2. Log the Security Breach
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String actionMessage = "CRITICAL: System Locked due to potential Brute Force/Data Breach detection.";
        
        if ("ADMIN_MANUAL".equals(userType)) {
            actionMessage = "MANUAL LOCKDOWN: Administrator triggered emergency system deactivation.";
        }

        SystemLog breachLog = new SystemLog(
            identifier, 
            userType, 
            timestamp, 
            actionMessage
        );
        mDatabase.child("system_logs").push().setValue(breachLog);

        // 3. Notify Admin via Email
        sendBreachNotificationEmail(identifier, userType, timestamp);
        
        Log.e(TAG, "SYSTEM LOCKDOWN TRIGGERED! Breach notification sent.");
    }

    private static void sendBreachNotificationEmail(String target, String type, String time) {
        String subject = "URGENT: Security Breach Detected - Hostel Application";
        String body = "Dear Administrator,\n\n" +
                "The Hostel Application automated security system has detected a potential security event.\n\n" +
                "DETAILS:\n" +
                "- Event: Emergency Lockdown / Failed Attempts\n" +
                "- Targeted Account: " + target + " (" + type + ")\n" +
                "- Time: " + time + "\n\n" +
                "ACTION TAKEN:\n" +
                "- The System Kill Switch has been activated.\n" +
                "- All user sessions have been restricted.\n\n" +
                "Please log in to the Firebase Console to investigate.\n\n" +
                "Regards,\n" +
                "Hostel Security Module";

        new EmailSender(ADMIN_NOTIFICATION_EMAIL, subject, body).execute();
    }
}
