package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.ecostayapp.utils.AdminFixer;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText edtAdminEmail, edtAdminPassword;
    private Button btnAdminLogin;
    private TextView textBackToUserLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        initViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        edtAdminEmail = findViewById(R.id.edtAdminEmail);
        edtAdminPassword = findViewById(R.id.edtAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        textBackToUserLogin = findViewById(R.id.textBackToUserLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        textBackToUserLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });

        btnAdminLogin.setOnClickListener(v -> loginAsAdmin());
    }

    private void loginAsAdmin() {
        String email = edtAdminEmail.getText().toString().trim();
        String password = edtAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtAdminEmail.setError("Email is required");
            edtAdminEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtAdminPassword.setError("Password is required");
            edtAdminPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Sign in with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verify if user is admin
                            verifyAdminStatus(user.getUid());
                        }
                    } else {
                        showProgress(false);
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verifyAdminStatus(String userId) {
        // Check if user exists in admins collection
        db.collection("admins").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showProgress(false);
                    
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        Boolean isActive = documentSnapshot.getBoolean("isActive");
                        
                        if (isAdmin != null && isAdmin && (isActive == null || isActive)) {
                            // Valid admin - proceed to admin dashboard
                            Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                            
                            Intent intent = new Intent(this, AdminActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // User exists but is not active admin
                            mAuth.signOut();
                            Toast.makeText(this, "Your admin access has been revoked. Contact support.", 
                                         Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // User is not an admin - offer to fix it
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            showAdminFixDialog(currentUser);
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this, "Access Denied: This account does not have admin privileges.", 
                                         Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    mAuth.signOut();
                    Toast.makeText(this, "Error verifying admin status: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
    }

    private void showAdminFixDialog(FirebaseUser user) {
        String email = user.getEmail();
        String uid = user.getUid();
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔧 Fix Admin Privilege")
                .setMessage("❌ This account doesn't have admin privileges.\n\n" +
                          "📧 Email: " + email + "\n" +
                          "🆔 UID: " + uid + "\n\n" +
                          "Would you like to automatically create the admin document?")
                .setPositiveButton("🔧 Fix Automatically", (dialog, which) -> {
                    createAdminDocument(user);
                })
                .setNegativeButton("📝 Manual Setup", (dialog, which) -> {
                    showManualSetupInstructions(user);
                })
                .setNeutralButton("❌ Cancel", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Admin setup cancelled", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    private void createAdminDocument(FirebaseUser user) {
        showProgress(true);
        
        String email = user.getEmail();
        String uid = user.getUid();
        
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
                showProgress(false);
                Toast.makeText(this, "✅ Admin document created successfully!", Toast.LENGTH_SHORT).show();
                
                // Now proceed to admin dashboard
                Intent intent = new Intent(this, AdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(this, "❌ Failed to create admin document: " + e.getMessage(), 
                             Toast.LENGTH_LONG).show();
                showManualSetupInstructions(user);
            });
    }
    
    private void showManualSetupInstructions(FirebaseUser user) {
        String email = user.getEmail();
        String uid = user.getUid();
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📝 Manual Admin Setup")
                .setMessage("🔧 To fix admin privilege manually:\n\n" +
                          "1. Go to Firebase Console\n" +
                          "2. Firestore Database → Start collection\n" +
                          "3. Collection ID: admins\n" +
                          "4. Document ID: " + uid + "\n" +
                          "5. Add these fields:\n" +
                          "   • email: " + email + " (string)\n" +
                          "   • name: EcoStay Admin (string)\n" +
                          "   • isAdmin: true (boolean)\n" +
                          "   • isActive: true (boolean)\n" +
                          "   • role: admin (string)\n" +
                          "6. Click 'Save'\n" +
                          "7. Try logging in again")
                .setPositiveButton("Got it!", (dialog, which) -> {
                    mAuth.signOut();
                })
                .setNegativeButton("Try Auto Fix", (dialog, which) -> {
                    createAdminDocument(user);
                })
                .show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAdminLogin.setEnabled(!show);
    }
}

