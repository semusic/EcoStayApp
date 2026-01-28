package com.example.ecostayapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecuritySettingsActivity extends AppCompatActivity {

    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private Button btnUpdatePassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);

        initViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void updatePassword() {
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(currentPassword)) {
            edtCurrentPassword.setError("Current password is required");
            edtCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("New password is required");
            edtNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Password must be at least 6 characters");
            edtNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        // Re-authenticate user with current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Update password
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                showProgress(false);
                                Toast.makeText(this, "Password updated successfully!", 
                                             Toast.LENGTH_SHORT).show();
                                
                                // Clear fields
                                edtCurrentPassword.setText("");
                                edtNewPassword.setText("");
                                edtConfirmPassword.setText("");
                                
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showProgress(false);
                                Toast.makeText(this, "Failed to update password: " + e.getMessage(), 
                                             Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Current password is incorrect", 
                                 Toast.LENGTH_SHORT).show();
                    edtCurrentPassword.setError("Incorrect password");
                    edtCurrentPassword.requestFocus();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdatePassword.setEnabled(!show);
    }
}








