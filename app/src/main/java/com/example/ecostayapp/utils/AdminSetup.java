package com.example.ecostayapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to set up admin users in Firestore
 * 
 * HOW TO CREATE AN ADMIN:
 * 1. First, register a regular user account with email/password
 * 2. Get the user's UID from Firebase Authentication console
 * 3. Call addAdmin(uid, email, name) to grant admin privileges
 * 
 * OR run this code once in your app to create an admin:
 * AdminSetup.createAdminAccount("admin@ecostay.com", "Admin Name", db);
 */
public class AdminSetup {

    /**
     * Add admin privileges to an existing user
     * 
     * @param userId The Firebase Auth UID of the user
     * @param email Admin email
     * @param name Admin name
     * @param db Firestore instance
     */
    public static void addAdmin(String userId, String email, String name, FirebaseFirestore db) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", email);
        adminData.put("name", name);
        adminData.put("isAdmin", true);
        adminData.put("isActive", true);
        adminData.put("role", "admin");
        adminData.put("createdAt", new Date());
        adminData.put("permissions", new String[]{
            "manage_rooms",
            "manage_bookings",
            "send_notifications",
            "view_analytics"
        });

        db.collection("admins").document(userId)
                .set(adminData)
                .addOnSuccessListener(aVoid -> 
                    android.util.Log.d("AdminSetup", "Admin user created successfully: " + email))
                .addOnFailureListener(e -> 
                    android.util.Log.e("AdminSetup", "Failed to create admin: " + e.getMessage()));
    }

    /**
     * Remove admin privileges from a user
     */
    public static void removeAdmin(String userId, FirebaseFirestore db) {
        db.collection("admins").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> 
                    android.util.Log.d("AdminSetup", "Admin privileges removed"))
                .addOnFailureListener(e -> 
                    android.util.Log.e("AdminSetup", "Failed to remove admin: " + e.getMessage()));
    }

    /**
     * Deactivate an admin (soft delete)
     */
    public static void deactivateAdmin(String userId, FirebaseFirestore db) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", false);
        updates.put("deactivatedAt", new Date());

        db.collection("admins").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> 
                    android.util.Log.d("AdminSetup", "Admin deactivated"))
                .addOnFailureListener(e -> 
                    android.util.Log.e("AdminSetup", "Failed to deactivate admin: " + e.getMessage()));
    }

    /**
     * Check if a user is an admin
     */
    public static void isAdmin(String userId, FirebaseFirestore db, AdminCheckCallback callback) {
        db.collection("admins").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        Boolean isActive = documentSnapshot.getBoolean("isActive");
                        callback.onResult(isAdmin != null && isAdmin && (isActive == null || isActive));
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * Create a complete admin account (for testing purposes)
     * This method creates both Firebase Auth user and admin privileges
     * 
     * USAGE: Call this once to create admin credentials
     */
    public static void createDefaultAdmin(FirebaseFirestore db) {
        // Default admin credentials
        String adminEmail = "admin@ecostay.com";
        String adminPassword = "admin123";
        String adminName = "EcoStay Admin";
        
        android.util.Log.d("AdminSetup", "Creating default admin account...");
        android.util.Log.d("AdminSetup", "Email: " + adminEmail);
        android.util.Log.d("AdminSetup", "Password: " + adminPassword);
        
        // Note: This will create the admin document in Firestore
        // You still need to create the Firebase Auth user manually
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", adminEmail);
        adminData.put("name", adminName);
        adminData.put("isAdmin", true);
        adminData.put("isActive", true);
        adminData.put("role", "admin");
        adminData.put("createdAt", new Date());
        adminData.put("permissions", new String[]{
            "manage_rooms",
            "manage_bookings", 
            "send_notifications",
            "view_analytics"
        });

        // Create a placeholder document - you'll need to replace the document ID with actual UID
        db.collection("admins").document("PLACEHOLDER_UID")
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("AdminSetup", "Admin document template created!");
                    android.util.Log.d("AdminSetup", "Next steps:");
                    android.util.Log.d("AdminSetup", "1. Register user with email: " + adminEmail);
                    android.util.Log.d("AdminSetup", "2. Get UID from Firebase Console");
                    android.util.Log.d("AdminSetup", "3. Replace 'PLACEHOLDER_UID' with actual UID");
                })
                .addOnFailureListener(e -> 
                    android.util.Log.e("AdminSetup", "Failed to create admin: " + e.getMessage()));
    }

    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }
}

