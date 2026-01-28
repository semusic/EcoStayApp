package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtCreateAccount, txtForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        txtCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Enter your email");
                edtEmail.requestFocus();
                return;
            }
            resetPassword(email);
        });

        btnLogin.setOnClickListener(v -> signInWithEmail());
        
        // Admin login link
        findViewById(R.id.textAdminLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
        });
    }

    private void signInWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Clear previous errors
        edtEmail.setError(null);
        edtPassword.setError(null);

        // Collect all errors
        StringBuilder errorMessages = new StringBuilder();
        boolean hasErrors = false;
        EditText firstErrorField = null;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtEmail;
            errorMessages.append("• Email is required\n");
            hasErrors = true;
        } else if (!isValidEmail(email)) {
            edtEmail.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtEmail;
            errorMessages.append("• Please enter a valid email address\n");
            hasErrors = true;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtPassword;
            errorMessages.append("• Password is required\n");
            hasErrors = true;
        } else if (password.length() < 6) {
            edtPassword.setError("* Too short");
            if (firstErrorField == null) firstErrorField = edtPassword;
            errorMessages.append("• Password must be at least 6 characters\n");
            hasErrors = true;
        }

        // If there are errors, show all at once
        if (hasErrors) {
            // Remove last newline
            if (errorMessages.length() > 0) {
                errorMessages.setLength(errorMessages.length() - 1);
            }
            
            final EditText errorFieldToFocus = firstErrorField;
            
            // Show error dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Sign In Errors")
                    .setMessage(errorMessages.toString())
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (errorFieldToFocus != null) {
                            errorFieldToFocus.requestFocus();
                        }
                    })
                    .show();
            return;
        }

        showProgress(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update FCM token on login
                            updateFCMToken(user.getUid());
                            
                            Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateFCMToken(String userId) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        
                        // Save token to Firestore
                        java.util.Map<String, Object> tokenData = new java.util.HashMap<>();
                        tokenData.put("fcmToken", token);
                        tokenData.put("lastLogin", new java.util.Date());
                        
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(tokenData)
                                .addOnFailureListener(e -> 
                                    android.util.Log.e("SignInActivity", "Failed to update FCM token", e));
                    }
                });
    }

    private void resetPassword(String email) {
        showProgress(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to send password reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private boolean isValidEmail(String email) {
        // Email validation pattern
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) {
            return "Sign in failed. Please try again.";
        }

        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            return "Sign in failed. Please try again.";
        }

        // Provide user-friendly error messages
        if (errorMessage.contains("There is no user record")) {
            return "No account found with this email. Please register first.";
        } else if (errorMessage.contains("The password is invalid")) {
            return "Incorrect password. Please try again.";
        } else if (errorMessage.contains("The email address is badly formatted")) {
            return "Invalid email format. Please enter a valid email address.";
        } else if (errorMessage.contains("A network error")) {
            return "Network error. Please check your internet connection and try again.";
        } else if (errorMessage.contains("The user account has been disabled")) {
            return "Your account has been disabled. Please contact support.";
        } else if (errorMessage.contains("Too many unsuccessful login attempts")) {
            return "Too many failed login attempts. Please try again later.";
        } else {
            return "Sign in failed: " + errorMessage;
        }
    }
}
