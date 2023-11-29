package com.example.coen390_safehit.controller;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.coen390_safehit.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class CriticalHitListener {

    private DatabaseReference criticalHitRef;
    private Context context;

    // Use your own channel ID and notification ID
    private static final String CHANNEL_ID = "COACH_CHANNEL";
    private int notificationId = 1; // Initial value for notification ID

    public CriticalHitListener(Context context, String macAddress, String playerName) {
        this.context = context; // Set the context

        // Initialize database reference (hard hit node)
        criticalHitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child(macAddress)
                .child("hit");

        criticalHitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);
                        if (hitValue != null) {
                            String[] hit = hitValue.split("\\|");
                            if (Double.parseDouble(hit[1]) <= 8) {
                                sendNotificationToCoach(DatabaseHelper.macAddress, playerName.toUpperCase() + ": Critical hit of " + hit[1] + " g detected", hit[0].split("@")[1]);
                                break;
                            }
                            Log.d("FIREBASE REAL-TIME EVENT DETECTED", "Critical hit of " + hitValue + " g detected, sending notification to coach...");
                        }
                    }
                } else {
                    // Handle the case where the 'Hard hit' node does not exist or is empty
                    Log.d("FirebaseData", "No hard hits recorded");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.w("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });

    }

    private void sendNotificationToCoach(String userId, String message, String timeStamp) {
        // Check for notification permission before attempting to send a notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Proceed with creating and sending the notification
        // Create a notification channel (for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ... existing channel creation code ...
        }

        // Generate a unique notification ID
        int uniqueNotificationId = notificationId++;  // Ensure notificationId is properly managed

        // Use the context to create notifications
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Critical Hit at " + timeStamp)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set a small icon for the notification
                .setSmallIcon(R.drawable.ic_check); // replace "ic_notification" with the name of your icon

        // Send the notification to the user
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(uniqueNotificationId, builder.build());
    }


}
