package com.example.group3ma;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // Verify a password against a hash
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
