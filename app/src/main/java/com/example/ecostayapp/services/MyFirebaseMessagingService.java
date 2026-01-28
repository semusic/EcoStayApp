package com.example.ecostayapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.ecostayapp.MainActivity;
import com.example.ecostayapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ecostay_notifications";
    private static final String CHANNEL_NAME = "EcoStay Notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            // Save notification to Firestore
            saveNotificationToFirestore(title, body, remoteMessage.getData());

            // Show notification
            sendNotification(title, body);
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String type = remoteMessage.getData().get("type");
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");

            if (title != null && message != null) {
                saveNotificationToFirestore(title, message, remoteMessage.getData());
                sendNotification(title, message);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Save token to Firestore for the current user
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("updatedAt", new Date());

            db.collection("users").document(userId)
                    .update(tokenData)
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "FCM token saved to Firestore"))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Failed to save FCM token", e));
        }
    }

    private void saveNotificationToFirestore(String title, String body, Map<String, String> data) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", body);
            notification.put("type", data.get("type") != null ? data.get("type") : "general");
            notification.put("read", false);
            notification.put("timestamp", new Date());
            notification.put("userId", userId);

            // Add additional data if present
            if (data.containsKey("offerId")) {
                notification.put("offerId", data.get("offerId"));
            }
            if (data.containsKey("activityId")) {
                notification.put("activityId", data.get("activityId"));
            }

            db.collection("notifications")
                    .add(notification)
                    .addOnSuccessListener(documentReference -> 
                        Log.d(TAG, "Notification saved with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Error saving notification", e));
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for EcoStay offers and activities");
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}








