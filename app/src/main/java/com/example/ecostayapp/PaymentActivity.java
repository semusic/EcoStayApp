package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostayapp.models.Booking;
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.models.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
// Removed Timestamp import due to resolution issues
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView textViewBookingSummary, textViewTotalAmount;
    private EditText edtCardNumber, edtExpiryDate, edtCVV, edtCardholderName;
    private Button btnPayNow;
    private ProgressBar progressBar;

    private String bookingType;
    private Room room;
    private Activity activity;
    private long checkInDate, checkOutDate;
    private int guests;
    private double totalPrice;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupFirebase();
        setupStripe();
        loadBookingData();
        setupClickListeners();
    }

    private void initViews() {
        textViewBookingSummary = findViewById(R.id.textViewBookingSummary);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        edtCVV = findViewById(R.id.edtCVV);
        edtCardholderName = findViewById(R.id.edtCardholderName);
        btnPayNow = findViewById(R.id.btnPayNow);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Add expiry date formatting
        setupExpiryDateFormatter();
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupStripe() {
        // Initialize Stripe with publishable key
        PaymentConfiguration.init(this, "pk_test_YOUR_PUBLISHABLE_KEY_HERE");
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    private void loadBookingData() {
        Intent intent = getIntent();
        bookingType = intent.getStringExtra("type");
        checkInDate = intent.getLongExtra("checkInDate", 0);
        checkOutDate = intent.getLongExtra("checkOutDate", 0);
        guests = intent.getIntExtra("guests", 1);
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0);

        if ("room".equals(bookingType)) {
            room = (Room) intent.getSerializableExtra("room");
            displayRoomBookingSummary();
        } else if ("activity".equals(bookingType)) {
            activity = (Activity) intent.getSerializableExtra("activity");
            displayActivityBookingSummary();
        }

        // Display in LKR
        textViewTotalAmount.setText("Total Amount: LKR " + String.format("%.0f", totalPrice));
    }

    private void displayRoomBookingSummary() {
        if (room != null) {
            Calendar checkIn = Calendar.getInstance();
            checkIn.setTimeInMillis(checkInDate);
            Calendar checkOut = Calendar.getInstance();
            checkOut.setTimeInMillis(checkOutDate);

            String summary = "Room: " + room.getName() + "\n" +
                    "Check In: " + String.format("%02d/%02d/%04d", 
                        checkIn.get(Calendar.DAY_OF_MONTH),
                        checkIn.get(Calendar.MONTH) + 1,
                        checkIn.get(Calendar.YEAR)) + "\n" +
                    "Check Out: " + String.format("%02d/%02d/%04d", 
                        checkOut.get(Calendar.DAY_OF_MONTH),
                        checkOut.get(Calendar.MONTH) + 1,
                        checkOut.get(Calendar.YEAR)) + "\n" +
                    "Guests: " + guests;

            textViewBookingSummary.setText(summary);
        }
    }

    private void displayActivityBookingSummary() {
        if (activity != null) {
            Calendar activityDate = Calendar.getInstance();
            activityDate.setTimeInMillis(checkInDate);

            String summary = "Activity: " + activity.getName() + "\n" +
                    "Date: " + String.format("%02d/%02d/%04d", 
                        activityDate.get(Calendar.DAY_OF_MONTH),
                        activityDate.get(Calendar.MONTH) + 1,
                        activityDate.get(Calendar.YEAR)) + "\n" +
                    "Duration: " + activity.getDuration() + "\n" +
                    "Participants: " + guests;

            textViewBookingSummary.setText(summary);
        }
    }

    private void setupClickListeners() {
        btnPayNow.setOnClickListener(v -> {
            if (validatePaymentInputs()) {
                processPayment();
            }
        });
    }

    private boolean validatePaymentInputs() {
        // Clear previous errors
        edtCardNumber.setError(null);
        edtExpiryDate.setError(null);
        edtCVV.setError(null);
        edtCardholderName.setError(null);

        // Collect all errors
        StringBuilder errorMessages = new StringBuilder();
        boolean hasErrors = false;
        EditText firstErrorField = null;

        String cardNumber = edtCardNumber.getText().toString().trim().replaceAll("\\s+", "");
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String cvv = edtCVV.getText().toString().trim();
        String cardholderName = edtCardholderName.getText().toString().trim();

        // Validate card number
        if (TextUtils.isEmpty(cardNumber)) {
            edtCardNumber.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtCardNumber;
            errorMessages.append("• Card number is required\n");
            hasErrors = true;
        } else if (!isValidCardNumber(cardNumber)) {
            edtCardNumber.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtCardNumber;
            errorMessages.append("• Please enter a valid 16-digit card number\n");
            hasErrors = true;
        } else if (!passesLuhnCheck(cardNumber)) {
            edtCardNumber.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtCardNumber;
            errorMessages.append("• Invalid card number. Please check your card details\n");
            hasErrors = true;
        }

        // Validate expiry date
        if (TextUtils.isEmpty(expiryDate)) {
            edtExpiryDate.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtExpiryDate;
            errorMessages.append("• Expiry date is required\n");
            hasErrors = true;
        } else if (!isValidExpiryDate(expiryDate)) {
            edtExpiryDate.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtExpiryDate;
            errorMessages.append("• Invalid expiry date (use MM/YY or MM/YYYY format). Entered: '" + expiryDate + "'\n");
            hasErrors = true;
        } else if (isCardExpired(expiryDate)) {
            edtExpiryDate.setError("* Expired");
            if (firstErrorField == null) firstErrorField = edtExpiryDate;
            errorMessages.append("• Card has expired. Please use a valid card\n");
            hasErrors = true;
        }

        // Validate CVV
        if (TextUtils.isEmpty(cvv)) {
            edtCVV.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtCVV;
            errorMessages.append("• CVV is required\n");
            hasErrors = true;
        } else if (!isValidCVV(cvv)) {
            edtCVV.setError("* Invalid");
            if (firstErrorField == null) firstErrorField = edtCVV;
            errorMessages.append("• CVV must be 3 or 4 digits\n");
            hasErrors = true;
        }

        // Validate cardholder name
        if (TextUtils.isEmpty(cardholderName)) {
            edtCardholderName.setError("* Required");
            if (firstErrorField == null) firstErrorField = edtCardholderName;
            errorMessages.append("• Cardholder name is required\n");
            hasErrors = true;
        } else if (cardholderName.length() < 3) {
            edtCardholderName.setError("* Too short");
            if (firstErrorField == null) firstErrorField = edtCardholderName;
            errorMessages.append("• Cardholder name must be at least 3 characters\n");
            hasErrors = true;
        }

        // Show all errors if any exist
        if (hasErrors) {
            if (errorMessages.length() > 0) {
                errorMessages.setLength(errorMessages.length() - 1);
            }
            
            final EditText errorFieldToFocus = firstErrorField;
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Payment Validation Errors")
                    .setMessage(errorMessages.toString())
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (errorFieldToFocus != null) {
                            errorFieldToFocus.requestFocus();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    private void processPayment() {
        showProgress(true);

        String cardNumber = edtCardNumber.getText().toString().trim().replaceAll("\\s+", "");

        // Simulate different payment scenarios based on card number
        new android.os.Handler().postDelayed(() -> {
            showProgress(false);
            
            // Test card numbers for different scenarios:
            // 4242424242424242 = Success
            // 4000000000000002 = Declined (insufficient funds)
            // 4000000000000127 = Declined (incorrect CVC)
            // Any other invalid = Card declined
            
            if (cardNumber.equals("4242424242424242")) {
                // Success - Standard test card
                saveBooking();
            } else if (cardNumber.equals("4000000000000002")) {
                // Insufficient funds
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Payment Failed")
                        .setMessage("Insufficient funds. Please use another payment method.")
                        .setPositiveButton("OK", null)
                        .show();
            } else if (cardNumber.equals("4000000000000127")) {
                // Incorrect CVC
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Payment Failed")
                        .setMessage("Incorrect CVV/CVC. Please check your card details.")
                        .setPositiveButton("OK", null)
                        .show();
            } else if (cardNumber.startsWith("4")) {
                // Other Visa cards - simulate random success/failure
                if (Math.random() > 0.3) {
                    saveBooking();
                } else {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Payment Failed")
                            .setMessage("Payment declined by your bank. Please try another card.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            } else {
                // Invalid card or declined
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Payment Failed")
                        .setMessage("Payment failed. Please check your card details and try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }, 2000);
    }

    private void saveBooking() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Date checkInDateObj = new Date(checkInDate);
        Date checkOutDateObj = new Date(checkOutDate);
        
        // Debug logging
        android.util.Log.d("PaymentActivity", "=== SAVING BOOKING DATES ===");
        android.util.Log.d("PaymentActivity", "CheckIn timestamp: " + checkInDate);
        android.util.Log.d("PaymentActivity", "CheckIn Date object: " + checkInDateObj);
        android.util.Log.d("PaymentActivity", "CheckOut timestamp: " + checkOutDate);
        android.util.Log.d("PaymentActivity", "CheckOut Date object: " + checkOutDateObj);
        android.util.Log.d("PaymentActivity", "BookingDate: " + new Date() + " (current timestamp: " + System.currentTimeMillis() + ")");

        Booking booking;
        if ("room".equals(bookingType)) {
            booking = new Booking(userId, room.getId(), "room", checkInDateObj, checkOutDateObj, guests, totalPrice);
        } else {
            booking = new Booking(userId, activity.getId(), "activity", checkInDateObj, checkOutDateObj, guests, totalPrice);
            booking.setActivityId(activity.getId());
        }

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("userId", booking.getUserId());
        bookingData.put("type", booking.getType());
        // Use regular Date objects for storage
        bookingData.put("checkInDate", booking.getCheckInDate());
        bookingData.put("checkOutDate", booking.getCheckOutDate());
        bookingData.put("guests", booking.getGuests());
        bookingData.put("totalPrice", booking.getTotalPrice());
        bookingData.put("status", booking.getStatus());
        bookingData.put("bookingDate", booking.getBookingDate());
        bookingData.put("paymentStatus", booking.getPaymentStatus());
        bookingData.put("paymentId", "demo_payment_" + System.currentTimeMillis());

        if (room != null) {
            bookingData.put("roomId", room.getId());
        }
        if (activity != null) {
            bookingData.put("activityId", activity.getId());
        }

        android.util.Log.d("PaymentActivity", "About to save booking data to Firebase:");
        android.util.Log.d("PaymentActivity", "bookingData: " + bookingData);
        
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("PaymentActivity", "Booking saved successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Booking confirmed! Payment successful.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PaymentActivity", "Error saving booking", e);
                    Toast.makeText(this, "Failed to save booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            saveBooking();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment failed: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPayNow.setEnabled(!show);
    }

    // Card validation methods
    private boolean isValidCardNumber(String cardNumber) {
        // Check if it's 13-19 digits (most cards are 16)
        return cardNumber.matches("\\d{13,19}");
    }

    private boolean passesLuhnCheck(String cardNumber) {
        // Luhn algorithm to validate card number
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    private boolean isValidExpiryDate(String expiryDate) {
        // Remove any spaces and trim
        expiryDate = expiryDate.trim();
        
        // Check if it contains a slash
        if (!expiryDate.contains("/")) {
            return false;
        }
        
        // Split by slash and validate parts
        String[] parts = expiryDate.split("/");
        if (parts.length != 2) {
            return false;
        }
        
        try {
            // Validate month (01-12)
            int month = Integer.parseInt(parts[0]);
            if (month < 1 || month > 12) {
                return false;
            }
            
            // Validate year (2 or 4 digits)
            String yearStr = parts[1];
            if (yearStr.length() == 2) {
                // 2-digit year (00-99)
                int year = Integer.parseInt(yearStr);
                return year >= 0 && year <= 99;
            } else if (yearStr.length() == 4) {
                // 4-digit year (should be reasonable)
                int year = Integer.parseInt(yearStr);
                return year >= 2024 && year <= 2099;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isCardExpired(String expiryDate) {
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            String yearStr = parts[1];
            
            int year;
            if (yearStr.length() == 2) {
                // Convert YY to YYYY (assume 20XX for years 00-99)
                year = Integer.parseInt(yearStr) + 2000;
            } else if (yearStr.length() == 4) {
                // Already in YYYY format
                year = Integer.parseInt(yearStr);
            } else {
                return true; // Invalid year format
            }
            
            Calendar expiry = Calendar.getInstance();
            expiry.set(year, month - 1, 1); // Month is 0-indexed
            expiry.set(Calendar.DAY_OF_MONTH, expiry.getActualMaximum(Calendar.DAY_OF_MONTH));
            
            Calendar now = Calendar.getInstance();
            
            return expiry.before(now);
        } catch (Exception e) {
            return true; // Treat parse errors as expired
        }
    }

    private boolean isValidCVV(String cvv) {
        // CVV should be 3 or 4 digits
        return cvv.matches("\\d{3,4}");
    }
    
    private void setupExpiryDateFormatter() {
        edtExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().replaceAll("[^\\d]", ""); // Remove non-digits
                String formatted = "";
                
                if (input.length() >= 2) {
                    formatted = input.substring(0, 2) + "/" + input.substring(2);
                } else if (input.length() > 0) {
                    formatted = input;
                }
                
                if (!formatted.equals(s.toString())) {
                    edtExpiryDate.setText(formatted);
                    edtExpiryDate.setSelection(formatted.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
