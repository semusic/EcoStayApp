package com.example.ecostayapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.adapters.NotificationsAdapter;
import com.example.ecostayapp.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter notificationsAdapter;
    private List<Notification> notifications = new ArrayList<>();
    private LinearLayout layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        notificationsAdapter = new NotificationsAdapter(notifications, notification -> {
            // Handle notification click
            Toast.makeText(this, "Notification: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationsAdapter);
    }

    private void loadNotifications() {
        // Clear existing notifications
        notifications.clear();

        // Add sample notifications
        notifications.add(new Notification(
            "Booking Confirmed",
            "Your room booking for Mountain View Cabin is confirmed for tomorrow!",
            "2 hours ago",
            true
        ));

        notifications.add(new Notification(
            "New Activity Available",
            "Mountain Hiking Adventure is now available. Book your spot!",
            "5 hours ago",
            true
        ));

        notifications.add(new Notification(
            "Special Offer",
            "Get 20% off on all eco-tours this week. Limited time offer!",
            "1 day ago",
            false
        ));

        notifications.add(new Notification(
            "Check-out Reminder",
            "Your check-out time is 11:00 AM tomorrow. Please prepare accordingly.",
            "2 days ago",
            false
        ));

        notifications.add(new Notification(
            "Weather Update",
            "Perfect weather conditions for outdoor activities today!",
            "3 days ago",
            false
        ));

        notifications.add(new Notification(
            "Welcome to EcoStay",
            "Thank you for choosing EcoStay Retreat. Enjoy your eco-friendly stay!",
            "1 week ago",
            false
        ));

        // Update UI
        notificationsAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (notifications.isEmpty()) {
            recyclerViewNotifications.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}







