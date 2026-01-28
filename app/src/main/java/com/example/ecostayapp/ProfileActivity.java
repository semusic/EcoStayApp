package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile, btnNotifications, btnBack;
    private TextView textViewName;
    private LinearLayout layoutUserInfo, layoutPersonalInfo, 
                         layoutPayments, layoutLoginSecurity;
    private Button btnLogout;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupFirebase();
        setupClickListeners();
        loadUserProfile();
    }

    private void initViews() {
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewName = findViewById(R.id.textViewName);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnBack = findViewById(R.id.btnBack);
        layoutUserInfo = findViewById(R.id.layoutUserInfo);
        layoutPersonalInfo = findViewById(R.id.layoutPersonalInfo);
        layoutPayments = findViewById(R.id.layoutPayments);
        layoutLoginSecurity = findViewById(R.id.layoutLoginSecurity);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to previous activity (MainActivity)
        });

        // Notifications click listener
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });

        // User Info section click listener (Show profile details)
        layoutUserInfo.setOnClickListener(v -> {
            showUserProfileDetails();
        });

        // Personal Information click listener
        layoutPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonalInfoActivity.class);
            startActivity(intent);
        });

        // Payments and Payouts click listener
        layoutPayments.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentsActivity.class);
            startActivity(intent);
        });

        // Login & Security click listener
        layoutLoginSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecuritySettingsActivity.class);
            startActivity(intent);
        });

        // Log out button click listener
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showUserProfileDetails() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Details");
        
        String message = "Name: " + textViewName.getText().toString() + "\n" +
                        "Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "Not available") + "\n" +
                        "User ID: " + currentUser.getUid();
        
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log out");
        builder.setMessage("Are you sure you want to log out?");
        
        builder.setPositiveButton("Log out", (dialog, which) -> {
            logoutUser();
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logoutUser() {
        showProgress(true);
        
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate back to SignIn activity
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            showProgress(false);
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgress(true);

        // Load user info from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showProgress(false);
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        textViewName.setText(fullName != null ? fullName : "User");
                    } else {
                        // User document doesn't exist, use basic info
                        textViewName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    // Use basic info if Firestore fails
                    textViewName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
                    Toast.makeText(this, "Failed to load profile details", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
