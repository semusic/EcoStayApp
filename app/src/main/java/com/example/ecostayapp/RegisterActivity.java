package com.example.ecostayapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtDateOfBirth, edtPhoneNumber, edtPassword, edtConfirmPassword;
    private AutoCompleteTextView spinnerGender;
    private Button btnCreateAccount;
    private CheckBox chkTerms;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtDateOfBirth = findViewById(R.id.edtDateOfBirth);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        chkTerms = findViewById(R.id.chkTerms);
        progressBar = findViewById(R.id.progressBar);
        
        // Setup gender dropdown
        setupGenderDropdown();
    }

    private void setupGenderDropdown() {
        String[] genders = new String[]{"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(adapter);
        
        spinnerGender.setOnItemClickListener((parent, view, position, id) -> {
            selectedGender = genders[position];
        });
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtDateOfBirth.setOnClickListener(v -> showDatePicker());

        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    edtDateOfBirth.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


    private void createAccount() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String dateOfBirth = edtDateOfBirth.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        selectedGender = spinnerGender.getText().toString().trim();

        // Clear all previous errors
        clearAllErrors();

        // Collect all errors
        StringBuilder errorMessages = new StringBuilder();
        boolean hasErrors = false;
        EditText firstErrorField = null;

        // Validate all fields and mark errors
        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtFullName;
            errorMessages.append("• Full name is required\n");
            hasErrors = true;
        } else if (fullName.length() < 3) {
            edtFullName.setError("* Too short");
            if (firstErrorField == null) firstErrorField = edtFullName;
            errorMessages.append("• Full name must be at least 3 characters\n");
            hasErrors = true;
        }

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

        if (TextUtils.isEmpty(dateOfBirth)) {
            edtDateOfBirth.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtDateOfBirth;
            errorMessages.append("• Date of birth is required\n");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(selectedGender)) {
            spinnerGender.setError("* Required");
            if (firstErrorField == null) firstErrorField = (EditText) spinnerGender;
            errorMessages.append("• Please select gender\n");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            edtPhoneNumber.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtPhoneNumber;
            errorMessages.append("• Phone number is required\n");
            hasErrors = true;
        } else if (!isValidPhoneNumber(phoneNumber)) {
            edtPhoneNumber.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtPhoneNumber;
            errorMessages.append("• Please enter a valid phone number (10-15 digits)\n");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtPassword;
            errorMessages.append("• Password is required\n");
            hasErrors = true;
        } else if (password.length() < 8) {
            edtPassword.setError("* Too short");
            if (firstErrorField == null) firstErrorField = edtPassword;
            errorMessages.append("• Password must be at least 8 characters\n");
            hasErrors = true;
        } else {
            String passwordStrength = checkPasswordStrength(password);
            if (!passwordStrength.equals("Strong")) {
                edtPassword.setError("* Weak");
                if (firstErrorField == null) firstErrorField = edtPassword;
                errorMessages.append("• Weak password. Use uppercase, lowercase, numbers & special characters\n");
                hasErrors = true;
            }
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtConfirmPassword;
            errorMessages.append("• Please confirm your password\n");
            hasErrors = true;
        } else if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("* Mismatch");
            if (firstErrorField == null) firstErrorField = edtConfirmPassword;
            errorMessages.append("• Passwords do not match\n");
            hasErrors = true;
        }

        if (!chkTerms.isChecked()) {
            errorMessages.append("• Please accept the terms and conditions\n");
            hasErrors = true;
        }

        // If there are errors, show all at once and focus first error field
        if (hasErrors) {
            // Remove last newline
            if (errorMessages.length() > 0) {
                errorMessages.setLength(errorMessages.length() - 1);
            }
            
            final EditText errorFieldToFocus = firstErrorField;
            
            // Show error dialog with all errors
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Registration Errors")
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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            saveUserData(user.getUid(), fullName, email, dateOfBirth, selectedGender, phoneNumber);
                        }
                    } else {
                        showProgress(false);
                        String errorMessage = getFirebaseErrorMessage(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId, String fullName, String email, String dateOfBirth, String gender, String phoneNumber) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("dateOfBirth", dateOfBirth);
        userData.put("gender", gender);
        userData.put("phoneNumber", phoneNumber);
        userData.put("createdAt", System.currentTimeMillis());

        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                        userData.put("fcmToken", tokenTask.getResult());
                    }

                    // Save user data with FCM token
                    db.collection("users").document(userId)
                            .set(userData)
                            .addOnCompleteListener(task -> {
                                showProgress(false);
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to save user data: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCreateAccount.setEnabled(!show);
    }

    private void clearAllErrors() {
        edtFullName.setError(null);
        edtEmail.setError(null);
        edtDateOfBirth.setError(null);
        spinnerGender.setError(null);
        edtPhoneNumber.setError(null);
        edtPassword.setError(null);
        edtConfirmPassword.setError(null);
    }

    private boolean isValidEmail(String email) {
        // Email validation pattern
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Remove any spaces, dashes, or parentheses
        String cleanPhone = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check if it contains only digits
        if (!cleanPhone.matches("\\d+")) {
            return false;
        }
        
        // Check length (10 digits for most countries, allow 10-15)
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15;
    }

    private String checkPasswordStrength(String password) {
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        // Check for uppercase, lowercase, digits, and special characters
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        // Determine password strength
        int criteriaCount = 0;
        if (hasUppercase) criteriaCount++;
        if (hasLowercase) criteriaCount++;
        if (hasDigit) criteriaCount++;
        if (hasSpecialChar) criteriaCount++;

        if (password.length() < 8) {
            return "Weak";
        } else if (criteriaCount >= 4) {
            return "Strong";
        } else if (criteriaCount >= 3) {
            return "Medium";
        } else {
            return "Weak";
        }
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) {
            return "Registration failed. Please try again.";
        }

        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            return "Registration failed. Please try again.";
        }

        // Provide user-friendly error messages
        if (errorMessage.contains("The email address is already in use")) {
            return "This email is already registered. Please sign in or use a different email.";
        } else if (errorMessage.contains("The email address is badly formatted")) {
            return "Invalid email format. Please enter a valid email address.";
        } else if (errorMessage.contains("Password should be at least 6 characters")) {
            return "Password is too short. Please use at least 8 characters.";
        } else if (errorMessage.contains("A network error")) {
            return "Network error. Please check your internet connection and try again.";
        } else if (errorMessage.contains("The given password is invalid")) {
            return "Invalid password. Password must be at least 6 characters.";
        } else {
            return "Registration failed: " + errorMessage;
        }
    }
}
