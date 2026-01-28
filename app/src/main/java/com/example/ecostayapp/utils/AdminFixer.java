package com.example.ecostayapp.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Quick fix tool for admin privilege issues
 * This will help create the admin document in Firestore
 */
public class AdminFixer {

    private static final String TAG = "AdminFixer";

    /**
     * Get the current user's UID for admin setup
     */
    public static void getCurrentUserUID() {
        Log.d(TAG, "=== GETTING CURRENT USER UID ===");
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();
            
            Log.d(TAG, "✅ Current user found!");
            Log.d(TAG, "📧 Email: " + email);
            Log.d(TAG, "🆔 UID: " + uid);
            Log.d(TAG, "");
            Log.d(TAG, "📝 TO FIX ADMIN PRIVILEGE:");
            Log.d(TAG, "1. Go to Firebase Console → Firestore Database");
            Log.d(TAG, "2. Click 'Start collection'");
            Log.d(TAG, "3. Collection ID: admins");
            Log.d(TAG, "4. Document ID: " + uid);
            Log.d(TAG, "5. Add these fields:");
            Log.d(TAG, "   - email: " + email + " (string)");
            Log.d(TAG, "   - name: EcoStay Admin (string)");
            Log.d(TAG, "   - isAdmin: true (boolean)");
            Log.d(TAG, "   - isActive: true (boolean)");
            Log.d(TAG, "   - role: admin (string)");
            Log.d(TAG, "6. Click 'Save'");
            Log.d(TAG, "");
            Log.d(TAG, "🎯 Copy this UID: " + uid);
        } else {
            Log.e(TAG, "❌ No user currently logged in!");
            Log.e(TAG, "📝 Please log in as admin first");
        }
    }

    /**
     * Automatically create admin document for current user
     */
    public static void createAdminDocument() {
        Log.d(TAG, "=== CREATING ADMIN DOCUMENT ===");
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.e(TAG, "❌ No user logged in! Please log in first.");
            return;
        }
        
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        
        Log.d(TAG, "👤 Creating admin document for: " + email);
        Log.d(TAG, "🆔 UID: " + uid);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Create admin document
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", email);
        adminData.put("name", "EcoStay Admin");
        adminData.put("isAdmin", true);
        adminData.put("isActive", true);
        adminData.put("role", "admin");
        adminData.put("createdAt", System.currentTimeMillis());
        
        db.collection("admins").document(uid)
            .set(adminData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Admin document created successfully!");
                Log.d(TAG, "🎉 You now have admin privileges!");
                Log.d(TAG, "📱 Try logging in again");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to create admin document: " + e.getMessage());
                Log.e(TAG, "🔧 Check Firestore rules and internet connection");
                
                // Show manual instructions
                Log.d(TAG, "");
                Log.d(TAG, "📝 MANUAL SETUP REQUIRED:");
                Log.d(TAG, "1. Go to Firebase Console");
                Log.d(TAG, "2. Firestore Database → Start collection");
                Log.d(TAG, "3. Collection ID: admins");
                Log.d(TAG, "4. Document ID: " + uid);
                Log.d(TAG, "5. Add the fields manually");
            });
    }

    /**
     * Check if current user has admin privileges
     */
    public static void checkCurrentUserAdminStatus() {
        Log.d(TAG, "=== CHECKING CURRENT USER ADMIN STATUS ===");
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.e(TAG, "❌ No user logged in!");
            return;
        }
        
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        
        Log.d(TAG, "👤 Checking admin status for: " + email);
        Log.d(TAG, "🆔 UID: " + uid);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("admins").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "✅ Admin document found!");
                    Log.d(TAG, "📧 Email: " + documentSnapshot.getString("email"));
                    Log.d(TAG, "👤 Name: " + documentSnapshot.getString("name"));
                    Log.d(TAG, "🛡️ Is Admin: " + documentSnapshot.getBoolean("isAdmin"));
                    Log.d(TAG, "✅ Is Active: " + documentSnapshot.getBoolean("isActive"));
                    
                    boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    boolean isActive = documentSnapshot.getBoolean("isActive");
                    
                    if (isAdmin && isActive) {
                        Log.d(TAG, "🎉 ADMIN PRIVILEGES CONFIRMED!");
                        Log.d(TAG, "✅ You should be able to access admin panel");
                    } else {
                        Log.w(TAG, "⚠️ Admin document exists but privileges are wrong");
                        Log.w(TAG, "📝 isAdmin should be true and isActive should be true");
                    }
                } else {
                    Log.e(TAG, "❌ No admin document found!");
                    Log.e(TAG, "📝 You need to create an admin document");
                    Log.e(TAG, "🔧 Use createAdminDocument() method or follow manual steps");
                    
                    // Show the UID for manual setup
                    Log.d(TAG, "");
                    Log.d(TAG, "📋 MANUAL SETUP INFO:");
                    Log.d(TAG, "Document ID (UID): " + uid);
                    Log.d(TAG, "Email: " + email);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to check admin status: " + e.getMessage());
            });
    }

    /**
     * Run complete admin fix process
     */
    public static void fixAdminPrivilege() {
        Log.d(TAG, "🔧 Starting admin privilege fix...");
        Log.d(TAG, "=".repeat(50));
        
        getCurrentUserUID();
        Log.d(TAG, "");
        checkCurrentUserAdminStatus();
        Log.d(TAG, "");
        
        Log.d(TAG, "=".repeat(50));
        Log.d(TAG, "✅ Admin fix check complete!");
        Log.d(TAG, "📱 Check logs above for next steps");
    }
}







