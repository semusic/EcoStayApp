package com.example.ecostayapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNotificationsActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextMessage;
    private RadioGroup radioGroupType;
    private RadioButton radioOffer, radioActivity, radioGeneral;
    private Button btnSendNotification;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextNotificationTitle);
        editTextMessage = findViewById(R.id.editTextNotificationMessage);
        radioGroupType = findViewById(R.id.radioGroupNotificationType);
        radioOffer = findViewById(R.id.radioOffer);
        radioActivity = findViewById(R.id.radioActivity);
        radioGeneral = findViewById(R.id.radioGeneral);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSendNotification.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String message = editTextMessage.getText().toString().trim();

            if (title.isEmpty()) {
                editTextTitle.setError("Title is required");
                editTextTitle.requestFocus();
                return;
            }

            if (message.isEmpty()) {
                editTextMessage.setError("Message is required");
                editTextMessage.requestFocus();
                return;
            }

            int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
            String type = "general";
            
            if (selectedTypeId == R.id.radioOffer) {
                type = "offer";
            } else if (selectedTypeId == R.id.radioActivity) {
                type = "activity";
            }

            sendNotificationToAllUsers(title, message, type);
        });
    }

    private void sendNotificationToAllUsers(String title, String message, String type) {
        showProgress(true);

        // Get all users
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userIds = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        userIds.add(document.getId());
                    }

                    if (userIds.isEmpty()) {
                        showProgress(false);
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create notification for each user
                    int[] successCount = {0};
                    int totalUsers = userIds.size();

                    for (String userId : userIds) {
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("title", title);
                        notification.put("message", message);
                        notification.put("type", type);
                        notification.put("read", false);
                        notification.put("timestamp", new Date());
                        notification.put("userId", userId);
                        notification.put("sentBy", "admin");

                        db.collection("notifications")
                                .add(notification)
                                .addOnSuccessListener(documentReference -> {
                                    successCount[0]++;
                                    
                                    if (successCount[0] == totalUsers) {
                                        showProgress(false);
                                        Toast.makeText(this, 
                                            "Notification sent to " + totalUsers + " users!", 
                                            Toast.LENGTH_LONG).show();
                                        
                                        // Clear fields
                                        editTextTitle.setText("");
                                        editTextMessage.setText("");
                                        radioGeneral.setChecked(true);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    showProgress(false);
                                    Toast.makeText(this, 
                                        "Failed to send notification: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, 
                        "Failed to fetch users: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendNotification.setEnabled(!show);
    }
}








