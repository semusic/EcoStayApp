package com.example.ecostayapp.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class to check if admin setup is correct
 * Use this to verify your Firebase configuration
 */
public class AdminSetupChecker {

    private static final String TAG = "AdminSetupChecker";

    /**
     * Check if Firebase Authentication is properly configured
     */
    public static void checkFirebaseAuth() {
        Log.d(TAG, "=== FIREBASE AUTHENTICATION CHECK ===");
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        if (auth == null) {
            Log.e(TAG, "❌ Firebase Auth is NULL - Check google-services.json");
            return;
        }
        
        Log.d(TAG, "✅ Firebase Auth initialized successfully");
        
        // Check current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "👤 Current user: " + currentUser.getEmail());
            Log.d(TAG, "🆔 Current user UID: " + currentUser.getUid());
        } else {
            Log.d(TAG, "👤 No user currently logged in");
        }
    }

    /**
     * Check if admin user exists in Firestore
     */
    public static void checkAdminSetup() {
        Log.d(TAG, "=== ADMIN SETUP CHECK ===");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (db == null) {
            Log.e(TAG, "❌ Firestore is NULL - Check Firebase configuration");
            return;
        }
        
        Log.d(TAG, "✅ Firestore initialized successfully");
        
        // Check if admins collection exists and has documents
        db.collection("admins")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "📊 Admins collection found");
                Log.d(TAG, "📄 Number of admin documents: " + queryDocumentSnapshots.size());
                
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.w(TAG, "⚠️ Admins collection is EMPTY!");
                    Log.w(TAG, "📝 You need to create an admin document in Firestore");
                    Log.w(TAG, "📝 Follow the setup instructions in admin_setup_instructions.md");
                } else {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d(TAG, "👑 Admin document ID: " + doc.getId());
                        Log.d(TAG, "📧 Email: " + doc.getString("email"));
                        Log.d(TAG, "👤 Name: " + doc.getString("name"));
                        Log.d(TAG, "🛡️ Is Admin: " + doc.getBoolean("isAdmin"));
                        Log.d(TAG, "✅ Is Active: " + doc.getBoolean("isActive"));
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to read admins collection: " + e.getMessage());
                Log.e(TAG, "🔧 Check Firestore rules and internet connection");
            });
    }

    /**
     * Test admin login with default credentials
     */
    public static void testAdminLogin(String email, String password) {
        Log.d(TAG, "=== TESTING ADMIN LOGIN ===");
        Log.d(TAG, "📧 Testing email: " + email);
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "✅ Login successful!");
                        Log.d(TAG, "🆔 User UID: " + user.getUid());
                        
                        // Now check if this user is admin
                        checkUserAdminStatus(user.getUid());
                        
                        // Sign out after test
                        auth.signOut();
                    }
                } else {
                    Log.e(TAG, "❌ Login failed: " + task.getException().getMessage());
                    Log.e(TAG, "🔧 Check if user exists in Firebase Authentication");
                }
            });
    }

    /**
     * Check if a specific user has admin privileges
     */
    private static void checkUserAdminStatus(String userId) {
        Log.d(TAG, "=== CHECKING ADMIN STATUS ===");
        Log.d(TAG, "🆔 Checking UID: " + userId);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("admins").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "✅ Admin document found!");
                    Log.d(TAG, "📧 Email: " + documentSnapshot.getString("email"));
                    Log.d(TAG, "🛡️ Is Admin: " + documentSnapshot.getBoolean("isAdmin"));
                    Log.d(TAG, "✅ Is Active: " + documentSnapshot.getBoolean("isActive"));
                    
                    boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    boolean isActive = documentSnapshot.getBoolean("isActive");
                    
                    if (isAdmin && isActive) {
                        Log.d(TAG, "🎉 ADMIN SETUP IS CORRECT!");
                    } else {
                        Log.w(TAG, "⚠️ Admin document exists but privileges are wrong");
                        Log.w(TAG, "📝 Make sure isAdmin=true and isActive=true");
                    }
                } else {
                    Log.e(TAG, "❌ No admin document found for this UID!");
                    Log.e(TAG, "📝 Create a document in 'admins' collection with this UID as document ID");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to check admin status: " + e.getMessage());
            });
    }

    /**
     * Run complete admin setup verification
     */
    public static void runCompleteCheck() {
        Log.d(TAG, "🔍 Starting complete admin setup verification...");
        Log.d(TAG, "=".repeat(50));
        
        checkFirebaseAuth();
        Log.d(TAG, "");
        checkAdminSetup();
        Log.d(TAG, "");
        testAdminLogin("admin@ecostay.com", "admin123");
        
        Log.d(TAG, "=".repeat(50));
        Log.d(TAG, "✅ Verification complete! Check logs above for any issues.");
    }
}







