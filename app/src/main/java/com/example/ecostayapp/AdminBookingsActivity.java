package com.example.ecostayapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.adapters.AdminBookingsAdapter;
import com.example.ecostayapp.models.Booking;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminBookingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView textViewTotalBookings, textViewConfirmedBookings, textViewPendingBookings;
    private RecyclerView recyclerViewBookings;
    private AdminBookingsAdapter bookingsAdapter;
    private List<Booking> bookings = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        loadBookings();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        textViewTotalBookings = findViewById(R.id.textViewTotalBookings);
        textViewConfirmedBookings = findViewById(R.id.textViewConfirmedBookings);
        textViewPendingBookings = findViewById(R.id.textViewPendingBookings);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        bookingsAdapter = new AdminBookingsAdapter(bookings, new AdminBookingsAdapter.OnBookingActionListener() {
            @Override
            public void onUpdateStatus(Booking booking, String newStatus) {
                updateBookingStatus(booking, newStatus);
            }

            @Override
            public void onViewDetails(Booking booking) {
                viewBookingDetails(booking);
            }
        });
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(bookingsAdapter);
    }

    private void loadBookings() {
        db.collection("bookings")
                .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookings.clear();
                    
                    int confirmedCount = 0;
                    int pendingCount = 0;
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        
                        // Fix date conversion issues from Firebase
                        fixBookingDates(booking, document);
                        
                        bookings.add(booking);
                        
                        if ("confirmed".equals(booking.getStatus())) {
                            confirmedCount++;
                        } else if ("pending".equals(booking.getStatus())) {
                            pendingCount++;
                        }
                    }
                    
                    bookingsAdapter.notifyDataSetChanged();
                    updateStatistics(bookings.size(), confirmedCount, pendingCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatistics(int totalBookings, int confirmedBookings, int pendingBookings) {
        textViewTotalBookings.setText(String.valueOf(totalBookings));
        textViewConfirmedBookings.setText(String.valueOf(confirmedBookings));
        textViewPendingBookings.setText(String.valueOf(pendingBookings));
    }

    private void updateBookingStatus(Booking booking, String newStatus) {
        db.collection("bookings").document(booking.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking status updated to " + newStatus, 
                                 Toast.LENGTH_SHORT).show();
                    booking.setStatus(newStatus);
                    bookingsAdapter.notifyDataSetChanged();
                    loadBookings(); // Refresh statistics
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update booking status: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void viewBookingDetails(Booking booking) {
        // Create and show booking details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_booking_details, null);
        
        // Initialize dialog views
        TextView textViewBookingStatus = dialogView.findViewById(R.id.textViewBookingStatus);
        TextView textViewBookingIdDetail = dialogView.findViewById(R.id.textViewBookingIdDetail);
        TextView textViewBookingDateDetail = dialogView.findViewById(R.id.textViewBookingDateDetail);
        TextView textViewPaymentStatus = dialogView.findViewById(R.id.textViewPaymentStatus);
        TextView textViewGuestName = dialogView.findViewById(R.id.textViewGuestName);
        TextView textViewGuestEmail = dialogView.findViewById(R.id.textViewGuestEmail);
        TextView textViewRoomNameDetail = dialogView.findViewById(R.id.textViewRoomNameDetail);
        TextView textViewRoomType = dialogView.findViewById(R.id.textViewRoomType);
        TextView textViewGuestsDetail = dialogView.findViewById(R.id.textViewGuestsDetail);
        TextView textViewCheckInDetail = dialogView.findViewById(R.id.textViewCheckInDetail);
        TextView textViewCheckOutDetail = dialogView.findViewById(R.id.textViewCheckOutDetail);
        TextView textViewTotalPriceDetail = dialogView.findViewById(R.id.textViewTotalPriceDetail);
        
        // Populate booking information
        textViewBookingIdDetail.setText("Booking ID: " + (booking.getId() != null ? booking.getId().substring(0, Math.min(8, booking.getId().length())) : "N/A"));
        textViewRoomType.setText("Type: " + (booking.getType() != null ? booking.getType().toUpperCase() : "Unknown"));
        textViewGuestsDetail.setText("Guests: " + booking.getGuests());
        textViewTotalPriceDetail.setText("Total: LKR " + String.format("%.2f", booking.getTotalPrice()));
        textViewPaymentStatus.setText("Payment: " + (booking.getPaymentStatus() != null ? booking.getPaymentStatus().toUpperCase() : "Unknown"));
        
        // Set status
        if (booking.getStatus() != null) {
            textViewBookingStatus.setText(booking.getStatus().toUpperCase());
            // Set status background color
            int statusBackgroundRes;
            switch (booking.getStatus().toLowerCase()) {
                case "confirmed":
                    statusBackgroundRes = R.drawable.rounded_status_background;
                    break;
                case "pending":
                    statusBackgroundRes = R.drawable.rounded_status_pending;
                    break;
                case "cancelled":
                    statusBackgroundRes = R.drawable.rounded_status_cancelled;
                    break;
                default:
                    statusBackgroundRes = R.drawable.rounded_status_background;
                    break;
            }
            textViewBookingStatus.setBackgroundResource(statusBackgroundRes);
        }
        
        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (booking.getBookingDate() != null && booking.getBookingDate().getTime() > 1000) {
            textViewBookingDateDetail.setText("Booked: " + sdf.format(booking.getBookingDate()));
        } else {
            textViewBookingDateDetail.setText("Booked: Not Set");
        }
        
        if (booking.getCheckInDate() != null && booking.getCheckInDate().getTime() > 1000) {
            textViewCheckInDetail.setText("Check-in: " + sdf.format(booking.getCheckInDate()));
        } else {
            textViewCheckInDetail.setText("Check-in: Not Set");
        }
        
        if (booking.getCheckOutDate() != null && booking.getCheckOutDate().getTime() > 1000) {
            textViewCheckOutDetail.setText("Check-out: " + sdf.format(booking.getCheckOutDate()));
        } else {
            textViewCheckOutDetail.setText("Check-out: Not Set");
        }
        
        // Load room/activity and guest details
        loadRoomDetails(booking, textViewRoomNameDetail);
        loadGuestDetails(booking, textViewGuestName, textViewGuestEmail);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void fixBookingDates(Booking booking, QueryDocumentSnapshot document) {
        // Fix check-in date
        if (document.getTimestamp("checkInDate") != null) {
            booking.setCheckInDate(document.getTimestamp("checkInDate").toDate());
        } else if (document.getDate("checkInDate") != null) {
            booking.setCheckInDate(document.getDate("checkInDate"));
        }
        
        // Fix check-out date
        if (document.getTimestamp("checkOutDate") != null) {
            booking.setCheckOutDate(document.getTimestamp("checkOutDate").toDate());
        } else if (document.getDate("checkOutDate") != null) {
            booking.setCheckOutDate(document.getDate("checkOutDate"));
        }
        
        // Fix booking date
        if (document.getTimestamp("bookingDate") != null) {
            booking.setBookingDate(document.getTimestamp("bookingDate").toDate());
        } else if (document.getDate("bookingDate") != null) {
            booking.setBookingDate(document.getDate("bookingDate"));
        }
    }
    
    private void loadRoomDetails(Booking booking, TextView textViewRoomName) {
        if ("room".equals(booking.getType()) && booking.getRoomId() != null) {
            db.collection("rooms").document(booking.getRoomId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String roomName = documentSnapshot.getString("name");
                        String category = documentSnapshot.getString("category");
                        if (roomName != null && !roomName.trim().isEmpty()) {
                            textViewRoomName.setText(roomName);
                            if (category != null && !category.trim().isEmpty()) {
                                textViewRoomName.append(" (" + category + ")");
                            }
                        } else {
                            textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())));
                        }
                    } else {
                        textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())) + " (Deleted)");
                    }
                })
                .addOnFailureListener(e -> {
                    textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())) + " (Error)");
                });
        } else if ("activity".equals(booking.getType()) && booking.getActivityId() != null) {
            db.collection("activities").document(booking.getActivityId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String activityName = documentSnapshot.getString("name");
                        if (activityName != null && !activityName.trim().isEmpty()) {
                            textViewRoomName.setText(activityName);
                        } else {
                            textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())));
                        }
                    } else {
                        textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())) + " (Deleted)");
                    }
                })
                .addOnFailureListener(e -> {
                    textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())) + " (Error)");
                });
        } else {
            textViewRoomName.setText("Unknown Booking Type");
        }
    }
    
    private void loadGuestDetails(Booking booking, TextView textViewGuestName, TextView textViewGuestEmail) {
        if (booking.getUserId() == null) {
            textViewGuestName.setText("Guest: Unknown");
            textViewGuestEmail.setText("Email: Not Available");
            return;
        }
        
        db.collection("users").document(booking.getUserId())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String email = documentSnapshot.getString("email");
                    
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        textViewGuestName.setText("Guest: " + fullName);
                    } else {
                        textViewGuestName.setText("Guest: User #" + booking.getUserId().substring(0, Math.min(8, booking.getUserId().length())));
                    }
                    
                    if (email != null && !email.trim().isEmpty()) {
                        textViewGuestEmail.setText("Email: " + email);
                    } else {
                        textViewGuestEmail.setText("Email: Not Available");
                    }
                } else {
                    textViewGuestName.setText("Guest: Unknown");
                    textViewGuestEmail.setText("Email: Not Available");
                }
            })
            .addOnFailureListener(e -> {
                textViewGuestName.setText("Guest: Error Loading");
                textViewGuestEmail.setText("Email: Error Loading");
            });
    }
}