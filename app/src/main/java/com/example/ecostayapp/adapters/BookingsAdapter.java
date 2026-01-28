package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private SimpleDateFormat dateFormat;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking, int position);
        void onViewDetails(Booking booking);
    }

    public BookingsAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewType;
        private TextView textViewDates;
        private TextView textViewGuests;
        private TextView textViewPrice;
        private TextView textViewStatus;
        private Button btnCancelBooking;
        private Button btnViewDetails;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewDates = itemView.findViewById(R.id.textViewDates);
            textViewGuests = itemView.findViewById(R.id.textViewGuests);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Booking booking) {
            String type = "room".equals(booking.getType()) ? "Room Booking" : "Activity Booking";
            textViewType.setText(type);

            String dates;
            if ("room".equals(booking.getType())) {
                String checkIn = booking.getCheckInDate() != null ? dateFormat.format(booking.getCheckInDate()) : "Not Set";
                String checkOut = booking.getCheckOutDate() != null ? dateFormat.format(booking.getCheckOutDate()) : "Not Set";
                dates = checkIn + " - " + checkOut;
            } else {
                dates = booking.getCheckInDate() != null ? dateFormat.format(booking.getCheckInDate()) : "Not Set";
            }
            textViewDates.setText(dates);

            textViewGuests.setText(booking.getGuests() + " " + ("room".equals(booking.getType()) ? "guests" : "participants"));
            textViewPrice.setText("LKR " + String.format("%.2f", booking.getTotalPrice()));

            // Set status with color
            String status = booking.getStatus();
            textViewStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");
            switch (status != null ? status.toLowerCase() : "") {
                case "confirmed":
                    textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_color));
                    break;
                case "pending":
                    textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                    break;
                case "cancelled":
                    textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_color));
                    break;
                default:
                    textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                    break;
            }

            // Show/hide cancel button based on status and date
            boolean canCancel = canCancelBooking(booking);
            btnCancelBooking.setVisibility(canCancel ? View.VISIBLE : View.GONE);

            // Set button click listeners
            btnCancelBooking.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking, getAdapterPosition());
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
                }
            });
        }

        private boolean canCancelBooking(Booking booking) {
            // Can cancel if:
            // 1. Status is "confirmed" or "pending"
            // 2. Check-in date is in the future (at least 24 hours away)
            
            if (booking.getStatus() == null) return false;
            
            String status = booking.getStatus().toLowerCase();
            if (!status.equals("confirmed") && !status.equals("pending")) {
                return false; // Already cancelled or other status
            }

            if (booking.getCheckInDate() == null) {
                return false;
            }

            // Check if check-in is at least 24 hours away
            long currentTime = System.currentTimeMillis();
            long checkInTime = booking.getCheckInDate().getTime();
            long hoursDifference = (checkInTime - currentTime) / (1000 * 60 * 60);

            return hoursDifference >= 24; // At least 24 hours before check-in
        }
    }
}
