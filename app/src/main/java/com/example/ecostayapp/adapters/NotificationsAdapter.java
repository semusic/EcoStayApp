package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationsAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewMessage;
        private TextView textViewTimestamp;
        private ImageView imageViewReadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            imageViewReadIndicator = itemView.findViewById(R.id.imageViewReadIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Notification notification = notifications.get(position);
                    listener.onNotificationClick(notification);
                    
                    // Mark as read when clicked
                    notification.setRead(true);
                    notifyItemChanged(position);
                }
            });
        }

        public void bind(Notification notification) {
            textViewTitle.setText(notification.getTitle());
            textViewMessage.setText(notification.getMessage());
            textViewTimestamp.setText(notification.getTimestamp());

            // Show/hide read indicator
            if (notification.isRead()) {
                imageViewReadIndicator.setVisibility(View.GONE);
                textViewTitle.setAlpha(0.7f);
                textViewMessage.setAlpha(0.7f);
            } else {
                imageViewReadIndicator.setVisibility(View.VISIBLE);
                textViewTitle.setAlpha(1.0f);
                textViewMessage.setAlpha(1.0f);
            }
        }
    }
}







