package com.example.ecostayapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.ecostayapp.adapters.ActivityImageSliderAdapter;
import com.example.ecostayapp.models.Activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ActivityBookingActivity extends AppCompatActivity {

    private ViewPager2 viewPagerActivityImages;
    private TextView textViewPageIndicator;
    private TextView textViewActivityName, textViewActivityDescription, textViewPrice, textViewTotalPrice;
    private EditText edtDate, edtParticipants;
    private Button btnBookNow;
    private ProgressBar progressBar;

    private Activity activity;
    private Calendar selectedDate;
    private ActivityImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_booking);

        initViews();
        loadActivityData();
        setupClickListeners();
    }

    private void initViews() {
        viewPagerActivityImages = findViewById(R.id.viewPagerActivityImages);
        textViewPageIndicator = findViewById(R.id.textViewPageIndicator);
        textViewActivityName = findViewById(R.id.textViewActivityName);
        textViewActivityDescription = findViewById(R.id.textViewActivityDescription);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        edtDate = findViewById(R.id.edtDate);
        edtParticipants = findViewById(R.id.edtParticipants);
        btnBookNow = findViewById(R.id.btnBookNow);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadActivityData() {
        activity = (Activity) getIntent().getSerializableExtra("activity");
        if (activity != null) {
            textViewActivityName.setText(activity.getName());
            textViewActivityDescription.setText(activity.getDescription());
            // Convert USD to LKR for display
            double priceLKR = activity.getPrice() * 325;
            textViewPrice.setText("LKR " + String.format("%.0f", priceLKR) + " per person");

            // Setup image slider with 4 beautiful activity images
            setupActivityImageSlider();

            edtParticipants.setText("1");
            
            // Initialize total price display
            calculateTotalPrice();
        }
    }

    private void setupActivityImageSlider() {
        // Create list with 4 stunning eco-activity images from Unsplash
        List<String> activityImages = new ArrayList<>();
        
        // Add 4 beautiful nature/activity images
        activityImages.add("https://images.unsplash.com/photo-1551632811-561732d1e306?w=800&q=80"); // Hiking in mountains
        activityImages.add("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=80"); // Mountain landscape
        activityImages.add("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80"); // Mountain trail
        activityImages.add("https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=800&q=80"); // Forest path
        
        // Setup adapter
        imageSliderAdapter = new ActivityImageSliderAdapter(this, activityImages);
        viewPagerActivityImages.setAdapter(imageSliderAdapter);

        // Setup page indicator
        updatePageIndicator(0, activityImages.size());

        // Add page change listener
        viewPagerActivityImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position, activityImages.size());
            }
        });
    }

    private void updatePageIndicator(int currentPage, int totalPages) {
        textViewPageIndicator.setText((currentPage + 1) + " / " + totalPages);
    }

    private void setupClickListeners() {
        edtDate.setOnClickListener(v -> showDatePicker());

        // Auto-calculate when participants number changes
        edtParticipants.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalPrice();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnBookNow.setOnClickListener(v -> {
            if (validateInputs()) {
                proceedToPayment();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    edtDate.setText(date);
                    
                    calculateTotalPrice();
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void calculateTotalPrice() {
        android.util.Log.d("ActivityBooking", "Calculating total price...");
        
        if (activity == null) {
            android.util.Log.e("ActivityBooking", "Activity is null!");
            textViewTotalPrice.setText("Error: Activity data missing");
            return;
        }
        
        String participantsText = edtParticipants.getText().toString().trim();
        
        if (participantsText.isEmpty()) {
            textViewTotalPrice.setText("Enter number of participants");
            return;
        }
        
        try {
            int participants = Integer.parseInt(participantsText);
            
            if (participants <= 0) {
                textViewTotalPrice.setText("Participants must be at least 1");
                return;
            }
            
            // Calculate in USD then convert to LKR
            double pricePerPersonUSD = activity.getPrice();
            double totalPriceUSD = pricePerPersonUSD * participants;
            double totalPriceLKR = totalPriceUSD * 325; // 1 USD = 325 LKR
            
            android.util.Log.d("ActivityBooking", "Price/person USD: " + pricePerPersonUSD);
            android.util.Log.d("ActivityBooking", "Participants: " + participants);
            android.util.Log.d("ActivityBooking", "Total LKR: " + totalPriceLKR);
            
            // Display detailed breakdown
            String priceBreakdown = String.format(java.util.Locale.getDefault(),
                "LKR %,.0f × %d person%s = LKR %,.0f",
                pricePerPersonUSD * 325,
                participants,
                participants > 1 ? "s" : "",
                totalPriceLKR
            );
            
            android.util.Log.d("ActivityBooking", "Displaying: " + priceBreakdown);
            textViewTotalPrice.setText(priceBreakdown);
            textViewTotalPrice.setTextColor(getResources().getColor(R.color.primary_color));
            textViewTotalPrice.setTextSize(20);
        } catch (NumberFormatException e) {
            android.util.Log.e("ActivityBooking", "Invalid number: " + participantsText);
            textViewTotalPrice.setText("Enter valid number of participants");
        }
    }

    private boolean validateInputs() {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }

        String participantsText = edtParticipants.getText().toString().trim();
        if (participantsText.isEmpty()) {
            Toast.makeText(this, "Please enter number of participants", Toast.LENGTH_SHORT).show();
            return false;
        }

        int participants = Integer.parseInt(participantsText);
        if (participants > activity.getMaxParticipants()) {
            Toast.makeText(this, "Maximum " + activity.getMaxParticipants() + " participants allowed", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (participants < 1) {
            Toast.makeText(this, "At least 1 participant required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void proceedToPayment() {
        int participants = Integer.parseInt(edtParticipants.getText().toString());
        double totalPrice = activity.getPrice() * participants;

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("type", "activity");
        intent.putExtra("activity", activity);
        intent.putExtra("checkInDate", selectedDate.getTimeInMillis());
        intent.putExtra("checkOutDate", selectedDate.getTimeInMillis()); // Same date for activities
        intent.putExtra("guests", participants);
        intent.putExtra("totalPrice", totalPrice);
        startActivity(intent);
    }
}
