package com.example.ecostayapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.adapters.BookingsAdapter;
import com.example.ecostayapp.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingsFragment extends Fragment {

    private RecyclerView recyclerViewBookings;
    private LinearLayout layoutEmptyState;
    private BookingsAdapter bookingsAdapter;
    private List<Booking> bookings = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadBookings();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        bookingsAdapter = new BookingsAdapter(bookings, new BookingsAdapter.OnBookingActionListener() {
            @Override
            public void onCancelBooking(Booking booking, int position) {
                showCancelConfirmationDialog(booking, position);
            }

            @Override
            public void onViewDetails(Booking booking) {
                showBookingDetailsDialog(booking);
            }
        });
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBookings.setAdapter(bookingsAdapter);
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() != null) {
            db.collection("bookings")
                    .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            bookings.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());
                                
                                // Fix dates if they are Firebase Timestamps
                                fixBookingDates(booking, document);
                                
                                bookings.add(booking);
                            }
                            bookingsAdapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                    });
        } else {
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (bookings.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewBookings.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewBookings.setVisibility(View.VISIBLE);
        }
    }
    
    private void fixBookingDates(Booking booking, QueryDocumentSnapshot document) {
        try {
            // Debug: Log what we get from Firebase
            android.util.Log.d("BookingsFragment", "=== DEBUGGING BOOKING DATES ===");
            android.util.Log.d("BookingsFragment", "Booking ID: " + booking.getId());
            
            // Fix check-in date
            Object checkInDateObj = document.get("checkInDate");
            android.util.Log.d("BookingsFragment", "checkInDate raw: " + checkInDateObj);
            android.util.Log.d("BookingsFragment", "checkInDate class: " + (checkInDateObj != null ? checkInDateObj.getClass().getSimpleName() : "null"));
            
            if (checkInDateObj != null) {
                if (checkInDateObj instanceof Date) {
                    android.util.Log.d("BookingsFragment", "checkInDate is Date: " + checkInDateObj);
                    booking.setCheckInDate((Date) checkInDateObj);
                } else if (checkInDateObj instanceof Long) {
                    android.util.Log.d("BookingsFragment", "checkInDate is Long: " + checkInDateObj);
                    booking.setCheckInDate(new Date((Long) checkInDateObj));
                } else {
                    android.util.Log.d("BookingsFragment", "checkInDate is other type: " + checkInDateObj.getClass().getName());
                    // Try to handle any other date format
                    if (checkInDateObj.getClass().getName().contains("Timestamp")) {
                        try {
                            Object dateObj = checkInDateObj.getClass().getMethod("toDate").invoke(checkInDateObj);
                            if (dateObj instanceof Date) {
                                android.util.Log.d("BookingsFragment", "Converted Timestamp to Date: " + dateObj);
                                booking.setCheckInDate((Date) dateObj);
                            }
                        } catch (Exception ex) {
                            android.util.Log.d("BookingsFragment", "Failed to convert Timestamp, using current date");
                            booking.setCheckInDate(new Date());
                        }
                    } else {
                        android.util.Log.d("BookingsFragment", "Unknown date type, using current date");
                        booking.setCheckInDate(new Date());
                    }
                }
            } else {
                android.util.Log.d("BookingsFragment", "checkInDate is null");
            }
            
            // Fix check-out date
            Object checkOutDateObj = document.get("checkOutDate");
            android.util.Log.d("BookingsFragment", "checkOutDate raw: " + checkOutDateObj);
            
            if (checkOutDateObj != null) {
                if (checkOutDateObj instanceof Date) {
                    booking.setCheckOutDate((Date) checkOutDateObj);
                } else if (checkOutDateObj instanceof Long) {
                    booking.setCheckOutDate(new Date((Long) checkOutDateObj));
                } else if (checkOutDateObj.getClass().getName().contains("Timestamp")) {
                    try {
                        Object dateObj = checkOutDateObj.getClass().getMethod("toDate").invoke(checkOutDateObj);
                        if (dateObj instanceof Date) {
                            booking.setCheckOutDate((Date) dateObj);
                        }
                    } catch (Exception ex) {
                        booking.setCheckOutDate(new Date());
                    }
                } else {
                    booking.setCheckOutDate(new Date());
                }
            }
            
            // Fix booking date
            Object bookingDateObj = document.get("bookingDate");
            android.util.Log.d("BookingsFragment", "bookingDate raw: " + bookingDateObj);
            
            if (bookingDateObj != null) {
                if (bookingDateObj instanceof Date) {
                    booking.setBookingDate((Date) bookingDateObj);
                } else if (bookingDateObj instanceof Long) {
                    booking.setBookingDate(new Date((Long) bookingDateObj));
                } else if (bookingDateObj.getClass().getName().contains("Timestamp")) {
                    try {
                        Object dateObj = bookingDateObj.getClass().getMethod("toDate").invoke(bookingDateObj);
                        if (dateObj instanceof Date) {
                            booking.setBookingDate((Date) dateObj);
                        }
                    } catch (Exception ex) {
                        booking.setBookingDate(new Date());
                    }
                } else {
                    booking.setBookingDate(new Date());
                }
            }
            
            // Final debug logging
            android.util.Log.d("BookingsFragment", "Final converted dates:");
            android.util.Log.d("BookingsFragment", "CheckIn: " + booking.getCheckInDate());
            android.util.Log.d("BookingsFragment", "CheckOut: " + booking.getCheckOutDate());
            android.util.Log.d("BookingsFragment", "BookingDate: " + booking.getBookingDate());
            android.util.Log.d("BookingsFragment", "=== END DEBUGGING ===");
            
        } catch (Exception e) {
            android.util.Log.e("BookingsFragment", "Error in fixBookingDates", e);
        }
    }

    private void showCancelConfirmationDialog(Booking booking, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String checkInDate = booking.getCheckInDate() != null ? sdf.format(booking.getCheckInDate()) : "N/A";
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?\n\n" +
                          "Check-in: " + checkInDate + "\n" +
                          "Total: LKR " + String.format("%.2f", booking.getTotalPrice()) + "\n\n" +
                          "Cancellation Policy:\n" +
                          "• Full refund if cancelled 24+ hours before check-in\n" +
                          "• Refund processed within 5-7 business days")
                .setPositiveButton("Cancel Booking", (dialog, which) -> {
                    cancelBooking(booking, position);
                })
                .setNegativeButton("Keep Booking", null)
                .show();
    }

    private void cancelBooking(Booking booking, int position) {
        if (booking.getId() == null) {
            Toast.makeText(getContext(), "Cannot cancel booking: Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update booking status to cancelled
        db.collection("bookings").document(booking.getId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), 
                            "Booking cancelled successfully. Refund will be processed within 5-7 business days.", 
                            Toast.LENGTH_LONG).show();
                    
                    // Update local data
                    booking.setStatus("cancelled");
                    bookingsAdapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), 
                            "Failed to cancel booking: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showBookingDetailsDialog(Booking booking) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        StringBuilder details = new StringBuilder();
        details.append("Booking ID: ").append(booking.getId() != null ? booking.getId().substring(0, Math.min(8, booking.getId().length())) : "N/A").append("\n\n");
        details.append("Type: ").append("room".equals(booking.getType()) ? "Room Booking" : "Activity Booking").append("\n");
        details.append("Check-in: ").append(booking.getCheckInDate() != null ? sdf.format(booking.getCheckInDate()) : "N/A").append("\n");
        
        if ("room".equals(booking.getType()) && booking.getCheckOutDate() != null) {
            details.append("Check-out: ").append(sdf.format(booking.getCheckOutDate())).append("\n");
        }
        
        details.append("Guests: ").append(booking.getGuests()).append("\n");
        details.append("Total Price: LKR ").append(String.format("%.2f", booking.getTotalPrice())).append("\n");
        details.append("Status: ").append(booking.getStatus() != null ? booking.getStatus().toUpperCase() : "UNKNOWN").append("\n");
        details.append("Payment: ").append(booking.getPaymentStatus() != null ? booking.getPaymentStatus().toUpperCase() : "UNKNOWN").append("\n");
        
        if (booking.getBookingDate() != null) {
            details.append("Booked on: ").append(sdf.format(booking.getBookingDate())).append("\n");
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Booking Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}