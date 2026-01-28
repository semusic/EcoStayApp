package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already logged in
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is logged in, check if they are admin
                checkAdminStatus(currentUser);
            } else {
                // User is not logged in, go to onboarding
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                finish();
            }
        }, SPLASH_DELAY);
    }

    private void checkAdminStatus(FirebaseUser user) {
        // First check if user is admin (check admins collection)
        db.collection("admins").document(user.getUid())
                .get()
                .addOnSuccessListener(adminSnapshot -> {
                    if (adminSnapshot.exists()) {
                        Boolean isAdmin = adminSnapshot.getBoolean("isAdmin");
                        Boolean isActive = adminSnapshot.getBoolean("isActive");
                        
                        if (isAdmin != null && isAdmin && (isActive == null || isActive)) {
                            // User is admin, go to admin activity
                            startActivity(new Intent(SplashActivity.this, AdminActivity.class));
                            finish();
                            return;
                        }
                    }
                    
                    // Not an admin or admin check failed, go to main activity for regular users
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error checking admin status, assume regular user
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
    }
}


