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
import com.example.ecostayapp.adapters.RoomImageSliderAdapter;
import com.example.ecostayapp.models.Booking;
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.utils.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RoomBookingActivity extends AppCompatActivity {

    private ViewPager2 viewPagerRoomImages;
    private TextView textViewPageIndicator;
    private TextView textViewRoomName, textViewRoomDescription, textViewPrice, textViewTotalPrice, textViewAmenities;
    private EditText edtCheckIn, edtCheckOut, edtGuests;
    private Button btnBookNow;
    private ProgressBar progressBar;

    private Room room;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Calendar checkInDate, checkOutDate;
    private RoomImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        initViews();
        setupFirebase();
        loadRoomData();
        setupClickListeners();
    }

    private void initViews() {
        viewPagerRoomImages = findViewById(R.id.viewPagerRoomImages);
        textViewPageIndicator = findViewById(R.id.textViewPageIndicator);
        textViewRoomName = findViewById(R.id.textViewRoomName);
        textViewRoomDescription = findViewById(R.id.textViewRoomDescription);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        textViewAmenities = findViewById(R.id.textViewAmenities);
        edtCheckIn = findViewById(R.id.edtCheckIn);
        edtCheckOut = findViewById(R.id.edtCheckOut);
        edtGuests = findViewById(R.id.edtGuests);
        btnBookNow = findViewById(R.id.btnBookNow);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void loadRoomData() {
        room = (Room) getIntent().getSerializableExtra("room");
        if (room != null) {
            android.util.Log.d("RoomBooking", "Room loaded: " + room.getName());
            android.util.Log.d("RoomBooking", "Price per night: " + room.getPricePerNight());
            android.util.Log.d("RoomBooking", "Max guests: " + room.getMaxGuests());
            
            textViewRoomName.setText(room.getName());
            textViewRoomDescription.setText(room.getDescription());
            
            // Display amenities
            if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
                textViewAmenities.setText(room.getAmenities());
            } else {
                textViewAmenities.setText("No amenities information available");
            }
            
            // Convert USD to LKR for display
            double pricePerNightUSD = room.getPricePerNight();
            if (pricePerNightUSD <= 0) {
                android.util.Log.e("RoomBooking", "Invalid price detected: " + pricePerNightUSD);
                // Use default price if invalid
                pricePerNightUSD = 45.0; // Default to $45 (LKR 14,625)
                android.util.Log.d("RoomBooking", "Using default price: " + pricePerNightUSD);
            }
            
            double priceLKR = pricePerNightUSD * 325;
            textViewPrice.setText("LKR " + String.format("%.0f", priceLKR) + "/night");
            android.util.Log.d("RoomBooking", "Display price: LKR " + String.format("%.0f", priceLKR));

            // Setup image slider
            setupImageSlider();

            edtGuests.setText(String.valueOf(room.getMaxGuests()));
            
            // Initialize total price display
            textViewTotalPrice.setText("Select check-in and check-out dates");
        } else {
            android.util.Log.e("RoomBooking", "Room is null!");
        }
    }

    private void setupClickListeners() {
        edtCheckIn.setOnClickListener(v -> showDatePicker(true));
        edtCheckOut.setOnClickListener(v -> showDatePicker(false));

        // Auto-calculate when guests number changes
        edtGuests.addTextChangedListener(new android.text.TextWatcher() {
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

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    
                    if (isCheckIn) {
                        edtCheckIn.setText(date);
                        checkInDate = selectedDate;
                        edtCheckOut.setText(""); // Reset check out when check in changes
                        checkOutDate = null;
                    } else {
                        if (checkInDate != null && selectedDate.before(checkInDate)) {
                            Toast.makeText(this, "Check out date must be after check in date", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        edtCheckOut.setText(date);
                        checkOutDate = selectedDate;
                    }
                    
                    calculateTotalPrice();
                }, year, month, day);

        if (!isCheckIn && checkInDate != null) {
            datePickerDialog.getDatePicker().setMinDate(checkInDate.getTimeInMillis());
        } else {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        
        datePickerDialog.show();
    }

    private void calculateTotalPrice() {
        android.util.Log.d("RoomBooking", "Calculating total price...");
        
        if (room == null) {
            android.util.Log.e("RoomBooking", "Room is null!");
            textViewTotalPrice.setText("Error: Room data missing");
            return;
        }
        
        if (checkInDate == null || checkOutDate == null) {
            android.util.Log.d("RoomBooking", "Dates not selected yet");
            textViewTotalPrice.setText("Select check-in and check-out dates");
            return;
        }
        
        // Calculate number of nights
        long diffInMillies = checkOutDate.getTimeInMillis() - checkInDate.getTimeInMillis();
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        
        android.util.Log.d("RoomBooking", "Check-in: " + checkInDate.getTime());
        android.util.Log.d("RoomBooking", "Check-out: " + checkOutDate.getTime());
        android.util.Log.d("RoomBooking", "Nights: " + diffInDays);
        
        if (diffInDays <= 0) {
            textViewTotalPrice.setText("Check-out must be after check-in");
            return;
        }
        
        // Get number of guests
        String guestsText = edtGuests.getText().toString().trim();
        int numberOfGuests = 1;
        try {
            if (!guestsText.isEmpty()) {
                numberOfGuests = Integer.parseInt(guestsText);
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("RoomBooking", "Invalid guests number: " + guestsText);
            numberOfGuests = 1;
        }
        
        // Calculate in USD then convert to LKR
        double pricePerNightUSD = room.getPricePerNight();
        if (pricePerNightUSD <= 0) {
            android.util.Log.e("RoomBooking", "Invalid price in calculation: " + pricePerNightUSD);
            pricePerNightUSD = 45.0; // Default to $45 (LKR 14,625)
            android.util.Log.d("RoomBooking", "Using default price in calculation: " + pricePerNightUSD);
        }
        
        double totalPriceUSD = pricePerNightUSD * diffInDays;
        double totalPriceLKR = totalPriceUSD * 325; // 1 USD = 325 LKR
        
        android.util.Log.d("RoomBooking", "Price/night USD: " + pricePerNightUSD);
        android.util.Log.d("RoomBooking", "Total USD: " + totalPriceUSD);
        android.util.Log.d("RoomBooking", "Total LKR: " + totalPriceLKR);
        
        // Display detailed breakdown
        String priceBreakdown = String.format(Locale.getDefault(),
            "LKR %,.0f × %d night%s = LKR %,.0f",
            pricePerNightUSD * 325,
            diffInDays,
            diffInDays > 1 ? "s" : "",
            totalPriceLKR
        );
        
        android.util.Log.d("RoomBooking", "Displaying: " + priceBreakdown);
        textViewTotalPrice.setText(priceBreakdown);
        textViewTotalPrice.setTextColor(getResources().getColor(R.color.primary_color));
        textViewTotalPrice.setTextSize(20);
    }

    private boolean validateInputs() {
        if (checkInDate == null) {
            Toast.makeText(this, "Please select check-in date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (checkOutDate == null) {
            Toast.makeText(this, "Please select check-out date", Toast.LENGTH_SHORT).show();
            return false;
        }

        String guestsText = edtGuests.getText().toString().trim();
        if (guestsText.isEmpty()) {
            Toast.makeText(this, "Please enter number of guests", Toast.LENGTH_SHORT).show();
            return false;
        }

        int guests = Integer.parseInt(guestsText);
        if (guests > room.getMaxGuests()) {
            Toast.makeText(this, "Maximum " + room.getMaxGuests() + " guests allowed", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void proceedToPayment() {
        long diffInMillies = checkOutDate.getTimeInMillis() - checkInDate.getTimeInMillis();
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        double totalPriceUSD = room.getPricePerNight() * diffInDays;
        // Convert to LKR for payment
        double totalPriceLKR = totalPriceUSD * 325;

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("type", "room");
        intent.putExtra("room", room);
        // Debug logging
        android.util.Log.d("RoomBooking", "=== PROCEEDING TO PAYMENT ===");
        android.util.Log.d("RoomBooking", "checkInDate Calendar: " + checkInDate.getTime());
        android.util.Log.d("RoomBooking", "checkInDate timestamp: " + checkInDate.getTimeInMillis());
        android.util.Log.d("RoomBooking", "checkOutDate Calendar: " + checkOutDate.getTime());
        android.util.Log.d("RoomBooking", "checkOutDate timestamp: " + checkOutDate.getTimeInMillis());
        
        intent.putExtra("checkInDate", checkInDate.getTimeInMillis());
        intent.putExtra("checkOutDate", checkOutDate.getTimeInMillis());
        intent.putExtra("guests", Integer.parseInt(edtGuests.getText().toString()));
        intent.putExtra("totalPrice", totalPriceLKR);
        startActivity(intent);
    }

    private void setupImageSlider() {
        // Get image URLs (use new imageUrls if available, fallback to single imageUrl)
        List<String> imageUrls = new ArrayList<>();
        
        if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
            // Use multiple images
            imageUrls.addAll(room.getImageUrls());
        } else if (room.getImageUrl() != null && !room.getImageUrl().isEmpty()) {
            // Fallback to single image for backward compatibility
            imageUrls.add(room.getImageUrl());
        } else {
            // No images available, use placeholder
            imageUrls.add(""); // Empty string will trigger placeholder in ImageLoader
        }

        // Setup adapter
        imageSliderAdapter = new RoomImageSliderAdapter(this, imageUrls);
        viewPagerRoomImages.setAdapter(imageSliderAdapter);

        // Setup page indicator
        updatePageIndicator(0, imageUrls.size());

        // Add page change listener
        viewPagerRoomImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position, imageUrls.size());
            }
        });
    }

    private void updatePageIndicator(int currentPage, int totalPages) {
        textViewPageIndicator.setText((currentPage + 1) + " / " + totalPages);
    }
}
