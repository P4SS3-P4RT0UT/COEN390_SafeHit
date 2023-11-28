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

import com.example.coen390_safehit.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class CriticalHitListener {

    private DatabaseReference criticalHitRef;
    DatabaseHelper database;
    Context context;

    // Use your own channel ID and notification ID
    private static final String CHANNEL_ID = "COACH_CHANNEL";
    private int notificationId = 1; // Initial value for notification ID

    public CriticalHitListener(Context context) {
        // Initialize the database helper
        this.context = context;
        database = DatabaseHelper.getInstance(this.context);
        // Initialize database reference (hard hit node)
        criticalHitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("08:D1:F9:A4:F7:38")
                .child("hit");

        // Read the data once
        criticalHitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);
                        if (hitValue != null) {
                            String[] hit = hitValue.split("\\|");
                            if (Double.parseDouble(hit[1]) <= 8) {
                                // Critical hit detected, trigger notification

                                // Get the coach of the player who received the hit

                                // sendNotificationToCoach(userID, "Your notification message");
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

    private void sendNotificationToCoach(String userId, String message) {
        // Create a notification channel (for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Generate a unique notification ID (could be a simple incremental value or a random number)
        int uniqueNotificationId = notificationId++;

        // Use the context to create notifications
        // For example:
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Critical Hit")
                .setContentText(message)
                // Add other notification properties as needed
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Send the notification to the user
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(uniqueNotificationId, builder.build());
    }
}
