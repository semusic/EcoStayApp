package com.example.ecostayapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onUpdateStatus(Booking booking, String newStatus);
        void onViewDetails(Booking booking);
    }

    public AdminBookingsAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewBookingId;
        private TextView textViewRoomName;
        private TextView textViewType;
        private TextView textViewCheckIn;
        private TextView textViewCheckOut;
        private TextView textViewGuests;
        private TextView textViewTotalPrice;
        private TextView textViewStatus;
        private TextView textViewBookingDate;
        private TextView textViewUserName;
        private Button btnConfirm;
        private Button btnCancel;
        private Button btnViewDetails;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBookingId = itemView.findViewById(R.id.textViewBookingId);
            textViewRoomName = itemView.findViewById(R.id.textViewRoomName);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewCheckIn = itemView.findViewById(R.id.textViewCheckIn);
            textViewCheckOut = itemView.findViewById(R.id.textViewCheckOut);
            textViewGuests = itemView.findViewById(R.id.textViewGuests);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewBookingDate = itemView.findViewById(R.id.textViewBookingDate);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Booking booking, OnBookingActionListener listener) {
            Log.d("AdminBookingsAdapter", "Binding booking: " + booking.getId() + ", status: " + booking.getStatus());
            
            textViewBookingId.setText("Booking #" + (booking.getId() != null ? booking.getId().substring(0, Math.min(8, booking.getId().length())) : "N/A"));
            textViewType.setText(booking.getType() != null ? booking.getType().toUpperCase() : "Unknown");
            textViewTotalPrice.setText("LKR " + String.format("%.2f", booking.getTotalPrice()));
            textViewStatus.setText(booking.getStatus() != null ? booking.getStatus().toUpperCase() : "UNKNOWN");
            
            // Load room/activity details
            loadItemDetails(booking);
            
            // Load user details
            loadUserDetails(booking.getUserId());
            
            // Format dates - handle both Date objects and timestamps
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            
            // Check-in date
            if (booking.getCheckInDate() != null) {
                Date checkInDate = booking.getCheckInDate();
                Log.d("AdminBookingsAdapter", "Check-in date: " + checkInDate + ", timestamp: " + checkInDate.getTime());
                // Check if it's a valid date (not epoch time)
                if (checkInDate.getTime() > 1000) { // More than 1 second from epoch
                    textViewCheckIn.setText("Check-in: " + sdf.format(checkInDate));
                } else {
                    textViewCheckIn.setText("Check-in: Not Set");
                }
            } else {
                Log.d("AdminBookingsAdapter", "Check-in date is null");
                textViewCheckIn.setText("Check-in: Not Set");
            }
            
            // Check-out date
            if (booking.getCheckOutDate() != null) {
                Date checkOutDate = booking.getCheckOutDate();
                if (checkOutDate.getTime() > 1000) {
                    textViewCheckOut.setText("Check-out: " + sdf.format(checkOutDate));
                } else {
                    textViewCheckOut.setText("Check-out: Not Set");
                }
            } else {
                textViewCheckOut.setText("Check-out: Not Set");
            }
            
            // Booking date
            if (booking.getBookingDate() != null) {
                Date bookingDate = booking.getBookingDate();
                if (bookingDate.getTime() > 1000) {
                    textViewBookingDate.setText("Booked: " + sdf.format(bookingDate));
                } else {
                    textViewBookingDate.setText("Booked: Not Set");
                }
            } else {
                textViewBookingDate.setText("Booked: Not Set");
            }
            
            textViewGuests.setText("Guests: " + booking.getGuests());
            
            // Set status background and text color
            int statusBackgroundRes;
            int statusTextColor;
            switch (booking.getStatus() != null ? booking.getStatus().toLowerCase() : "") {
                case "confirmed":
                    statusBackgroundRes = R.drawable.rounded_status_background; // Green
                    statusTextColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
                case "pending":
                    statusBackgroundRes = R.drawable.rounded_status_pending; // Orange
                    statusTextColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
                case "cancelled":
                    statusBackgroundRes = R.drawable.rounded_status_cancelled; // Red
                    statusTextColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
                default:
                    statusBackgroundRes = R.drawable.rounded_status_background; // Green default
                    statusTextColor = itemView.getContext().getResources().getColor(R.color.white);
                    break;
            }
            textViewStatus.setBackgroundResource(statusBackgroundRes);
            textViewStatus.setTextColor(statusTextColor);
            
            // Set button visibility based on status
            if ("pending".equals(booking.getStatus())) {
                btnViewDetails.setVisibility(View.VISIBLE);
                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnConfirm.setText("Confirm");
            } else {
                btnViewDetails.setVisibility(View.VISIBLE);
                btnConfirm.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
            }
            
            // Set click listeners
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(booking, "confirmed");
                }
            });
            
            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(booking, "cancelled");
                }
            });
            
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
                }
            });
        }
        
        private void loadItemDetails(Booking booking) {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            
            // Set default loading text
            if (textViewRoomName != null) {
                textViewRoomName.setText("Loading...");
            }
            
            if ("room".equals(booking.getType()) && booking.getRoomId() != null) {
                db.collection("rooms").document(booking.getRoomId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String roomName = documentSnapshot.getString("name");
                            String category = documentSnapshot.getString("category");
                            if (textViewRoomName != null) {
                                if (roomName != null && !roomName.trim().isEmpty()) {
                                    textViewRoomName.setText(roomName);
                                    if (category != null && !category.trim().isEmpty()) {
                                        textViewRoomName.append(" (" + category + ")");
                                    }
                                } else {
                                    textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())));
                                }
                            }
                        } else {
                            if (textViewRoomName != null) {
                                textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())) + " (Deleted)");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (textViewRoomName != null) {
                            textViewRoomName.setText("Room #" + booking.getRoomId().substring(0, Math.min(8, booking.getRoomId().length())) + " (Error)");
                        }
                    });
            } else if ("activity".equals(booking.getType()) && booking.getActivityId() != null) {
                db.collection("activities").document(booking.getActivityId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String activityName = documentSnapshot.getString("name");
                            if (textViewRoomName != null) {
                                if (activityName != null && !activityName.trim().isEmpty()) {
                                    textViewRoomName.setText(activityName);
                                } else {
                                    textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())));
                                }
                            }
                        } else {
                            if (textViewRoomName != null) {
                                textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())) + " (Deleted)");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (textViewRoomName != null) {
                            textViewRoomName.setText("Activity #" + booking.getActivityId().substring(0, Math.min(8, booking.getActivityId().length())) + " (Error)");
                        }
                    });
            } else {
                if (textViewRoomName != null) {
                    textViewRoomName.setText("Unknown Booking Type");
                }
            }
        }
        
        private void loadUserDetails(String userId) {
            if (userId == null) {
                if (textViewUserName != null) {
                    textViewUserName.setText("Guest: Unknown");
                }
                return;
            }
            
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        if (textViewUserName != null) {
                            if (fullName != null) {
                                textViewUserName.setText("Guest: " + fullName);
                            } else if (email != null) {
                                textViewUserName.setText("Guest: " + email);
                            } else {
                                textViewUserName.setText("Guest: User #" + userId.substring(0, Math.min(8, userId.length())));
                            }
                        }
                    } else {
                        if (textViewUserName != null) {
                            textViewUserName.setText("Guest: Unknown");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (textViewUserName != null) {
                        textViewUserName.setText("Guest: Error");
                    }
                });
        }
    }
}