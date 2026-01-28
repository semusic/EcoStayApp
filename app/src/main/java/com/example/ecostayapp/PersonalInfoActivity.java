package com.example.ecostayapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private EditText edtFullName, edtEmail, edtPhoneNumber, edtDateOfBirth;
    private AutoCompleteTextView spinnerGender;
    private Button btnSaveChanges, btnChangePhoto;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        initViews();
        setupFirebase();
        setupGenderDropdown();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        imageViewProfile = findViewById(R.id.imageViewProfile);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtDateOfBirth = findViewById(R.id.edtDateOfBirth);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupGenderDropdown() {
        String[] genders = new String[]{"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtDateOfBirth.setOnClickListener(v -> showDatePicker());

        btnChangePhoto.setOnClickListener(v -> {
            openImageChooser();
        });

        btnSaveChanges.setOnClickListener(v -> savePersonalInfo());
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

    private void loadUserData() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgress(true);

        // Set email (read-only)
        edtEmail.setText(currentUser.getEmail());

        // Load from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showProgress(false);
                    if (documentSnapshot.exists()) {
                        edtFullName.setText(documentSnapshot.getString("fullName"));
                        edtPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                        edtDateOfBirth.setText(documentSnapshot.getString("dateOfBirth"));
                        spinnerGender.setText(documentSnapshot.getString("gender"), false);
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Failed to load data: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void savePersonalInfo() {
        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String dateOfBirth = edtDateOfBirth.getText().toString().trim();
        String gender = spinnerGender.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Full name is required");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            edtPhoneNumber.setError("Phone number is required");
            edtPhoneNumber.requestFocus();
            return;
        }

        showProgress(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("dateOfBirth", dateOfBirth);
        updates.put("gender", gender);
        updates.put("updatedAt", System.currentTimeMillis());

        // First check if document exists, if not create it
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, update it
                        db.collection("users").document(currentUser.getUid())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    showProgress(false);
                                    Toast.makeText(this, "Personal information updated successfully!", 
                                                 Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showProgress(false);
                                    Toast.makeText(this, "Failed to update: " + e.getMessage(), 
                                                 Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document doesn't exist, create it with user data
                        updates.put("email", currentUser.getEmail());
                        updates.put("createdAt", System.currentTimeMillis());
                        
                        db.collection("users").document(currentUser.getUid())
                                .set(updates)
                                .addOnSuccessListener(aVoid -> {
                                    showProgress(false);
                                    Toast.makeText(this, "Personal information saved successfully!", 
                                                 Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showProgress(false);
                                    Toast.makeText(this, "Failed to save: " + e.getMessage(), 
                                                 Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Failed to check user data: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveChanges.setEnabled(!show);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewProfile.setImageBitmap(bitmap);
                uploadImageToFirebase(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        if (currentUser == null) return;

        showProgress(true);
        
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create storage reference
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("profile_images/" + currentUser.getUid() + ".jpg");

        // Upload image
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Update user document with image URL
                Map<String, Object> updates = new HashMap<>();
                updates.put("profileImageUrl", uri.toString());
                updates.put("updatedAt", System.currentTimeMillis());

                db.collection("users").document(currentUser.getUid())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            showProgress(false);
                            Toast.makeText(this, "Profile picture updated successfully!", 
                                         Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            showProgress(false);
                            Toast.makeText(this, "Failed to update profile picture: " + e.getMessage(), 
                                         Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            showProgress(false);
            Toast.makeText(this, "Failed to upload image: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
        });
    }
}



