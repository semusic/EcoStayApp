package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.adapters.ActivitiesAdapter;
import com.example.ecostayapp.models.Activity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivitiesCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView textViewSelectedDate;
    private RecyclerView recyclerViewActivities;
    private LinearLayout layoutEmptyState;
    
    private ActivitiesAdapter activitiesAdapter;
    private List<Activity> activities = new ArrayList<>();
    private FirebaseFirestore db;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_calendar);

        initViews();
        setupCalendar();
        setupRecyclerView();
        loadActivities();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        
        db = FirebaseFirestore.getInstance();
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupCalendar() {
        // Set minimum date to today
        calendarView.setMinDate(System.currentTimeMillis());
        
        // Set selected date to today
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(new Date(calendarView.getDate()));
        textViewSelectedDate.setText(selectedDate);
        
        // Listen for date changes
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Format selected date
                String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                
                // Create readable date
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
                    Date parsedDate = inputFormat.parse(date);
                    if (parsedDate != null) {
                        selectedDate = outputFormat.format(parsedDate);
                        textViewSelectedDate.setText(selectedDate);
                    }
                } catch (Exception e) {
                    selectedDate = date;
                    textViewSelectedDate.setText(date);
                }
                
                // Reload activities for selected date
                loadActivities();
            }
        });
    }

    private void setupRecyclerView() {
        activitiesAdapter = new ActivitiesAdapter(activities, activity -> {
            Intent intent = new Intent(this, ActivityBookingActivity.class);
            intent.putExtra("activity", activity);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewActivities.setAdapter(activitiesAdapter);
    }

    private void loadActivities() {
        db.collection("activities")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activities.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Activity activity = document.toObject(Activity.class);
                        activity.setId(document.getId());
                        activities.add(activity);
                    }
                    
                    if (activities.isEmpty()) {
                        showEmptyState();
                    } else {
                        showActivities();
                    }
                    
                    activitiesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load activities: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    loadSampleActivities();
                });
    }

    private void loadSampleActivities() {
        activities.clear();
        activities.add(new Activity("Guided Nature Hike", "Explore trails with expert guide", 45.0, "https://example.com/hike.jpg", "3 hours", 12, "Moderate", "Outdoor"));
        activities.add(new Activity("Eco Tour", "Learn about sustainable practices", 35.0, "https://example.com/ecotour.jpg", "2 hours", 15, "Easy", "Educational"));
        activities.add(new Activity("Bird Watching", "Observe local bird species", 30.0, "https://example.com/birds.jpg", "2 hours", 10, "Easy", "Wildlife"));
        activities.add(new Activity("Sunrise Yoga", "Morning yoga in nature", 20.0, "https://example.com/yoga.jpg", "1 hour", 25, "Easy", "Wellness"));
        
        showActivities();
        activitiesAdapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        recyclerViewActivities.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showActivities() {
        recyclerViewActivities.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }
}








