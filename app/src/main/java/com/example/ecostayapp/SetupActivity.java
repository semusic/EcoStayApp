package com.example.ecostayapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostayapp.utils.SampleDataGenerator;

/**
 * Setup activity to populate Firebase with sample data
 * This activity should only be used during development/testing
 */
public class SetupActivity extends AppCompatActivity {

    private Button btnAddRooms, btnAddInitiatives, btnAddActivities, btnAddAll;
    private SampleDataGenerator dataGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnAddRooms = findViewById(R.id.btnAddRooms);
        btnAddInitiatives = findViewById(R.id.btnAddInitiatives);
        btnAddActivities = findViewById(R.id.btnAddActivities);
        btnAddAll = findViewById(R.id.btnAddAll);
        
        dataGenerator = new SampleDataGenerator();
    }

    private void setupClickListeners() {
        btnAddRooms.setOnClickListener(v -> {
            dataGenerator.addSampleRooms();
            Toast.makeText(this, "Adding sample rooms to Firebase...", Toast.LENGTH_SHORT).show();
        });

        btnAddInitiatives.setOnClickListener(v -> {
            dataGenerator.addSampleEcoInitiatives();
            Toast.makeText(this, "Adding eco initiatives to Firebase...", Toast.LENGTH_SHORT).show();
        });

        btnAddActivities.setOnClickListener(v -> {
            dataGenerator.addSampleActivities();
            Toast.makeText(this, "Adding activities to Firebase...", Toast.LENGTH_SHORT).show();
        });

        btnAddAll.setOnClickListener(v -> {
            dataGenerator.addAllSampleData();
            Toast.makeText(this, "Adding all sample data to Firebase...", Toast.LENGTH_SHORT).show();
        });
    }
}







