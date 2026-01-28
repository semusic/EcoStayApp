package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Payment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentViewHolder> {

    private List<Payment> payments;

    public PaymentsAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewPaymentId;
        private TextView textViewAmount;
        private TextView textViewMethod;
        private TextView textViewStatus;
        private TextView textViewDate;
        private TextView textViewBookingId;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPaymentId = itemView.findViewById(R.id.textViewPaymentId);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewMethod = itemView.findViewById(R.id.textViewMethod);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewBookingId = itemView.findViewById(R.id.textViewBookingId);
        }

        public void bind(Payment payment) {
            textViewPaymentId.setText("Payment ID: " + payment.getId());
            textViewAmount.setText("LKR " + String.format("%.2f", payment.getAmount()));
            textViewMethod.setText(payment.getMethod());
            textViewStatus.setText(payment.getStatus());
            textViewBookingId.setText("Booking: " + payment.getBookingId());
            
            // Format date
            if (payment.getPaymentDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                textViewDate.setText(sdf.format(new Date(payment.getPaymentDate())));
            } else {
                textViewDate.setText("N/A");
            }
            
            // Set status color
            int statusColor;
            switch (payment.getStatus().toLowerCase()) {
                case "completed":
                    statusColor = itemView.getContext().getResources().getColor(R.color.eco_green_dark);
                    break;
                case "pending":
                    statusColor = itemView.getContext().getResources().getColor(R.color.accent_color);
                    break;
                case "failed":
                    statusColor = itemView.getContext().getResources().getColor(R.color.error_color);
                    break;
                default:
                    statusColor = itemView.getContext().getResources().getColor(R.color.text_secondary);
                    break;
            }
            textViewStatus.setTextColor(statusColor);
        }
    }
}
