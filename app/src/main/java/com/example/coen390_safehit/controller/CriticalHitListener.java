package com.example.coen390_safehit.controller;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CriticalHitListener {

    private DatabaseReference criticalHitRef;
    private Context context;

    // Use your own channel ID and notification ID
    private static final String CHANNEL_ID = "COACH_CHANNEL";
    private int notificationId = 1; // Initial value for notification ID

    public CriticalHitListener(Context context, String macAddress, String playerName, TextView lastHit) {
        this.context = context; // Set the context

        // Initialize database reference (hard hit node)
        criticalHitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child(macAddress)
                .child("hit");

        criticalHitRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);
                        if (hitValue != null) {
                            String[] hit = hitValue.split("\\|");
                            double threshold = Double.parseDouble(DatabaseHelper.threshold);
                            String dateTimeOfHit = hit[0];
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy@HH:mm:ss", Locale.getDefault());

                            try {
                                Date hitDate = sdf.parse(dateTimeOfHit);
                                long currentTime = System.currentTimeMillis();
                                long timeOfHit = hitDate.getTime();

                                // Check if the hit was recorded today and within the last 5 minutes
                                lastHit.setText("Last hit: " + hit[1] + " (" + sdf.format(hitDate).split("@")[1] + ", " + sdf.format(hitDate).split("@")[0] + ")");

                                if (isToday(hitDate) && (currentTime - timeOfHit <= 300000) && Double.parseDouble(hit[1]) > threshold) {
                                    // Send a notification to the coach
                                    lastHit.setTextColor(Color.YELLOW);
                                    sendNotificationToCoach(DatabaseHelper.macAddress, playerName.toUpperCase() + ": Critical hit of " + hit[1] + " G detected", sdf.format(hitDate));
                                    Log.d("FIREBASE REAL-TIME EVENT DETECTED", "Critical hit of " + hitValue + " G detected, sending notification to coach...");
                                    break;
                                } else {
                                    lastHit.setTextColor(Color.WHITE);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                } else {
                    // Handle the case where the 'Hard hit' node does not exist or is empty
                    Log.d("FirebaseData", "No hard hits recorded");
                }
            }

            // Helper method to check if the date is today
            private boolean isToday(Date date) {
                Calendar today = Calendar.getInstance();
                Calendar specifiedDate = Calendar.getInstance();
                specifiedDate.setTime(date);

                return today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == specifiedDate.get(Calendar.DAY_OF_YEAR);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Coach Channel";
            String description = "Channel for Coach Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("COACH_CHANNEL", name, importance);
            channel.setDescription(description);

            // Get the NotificationManager using context
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Generate a unique notification ID
        int uniqueNotificationId = notificationId++;  // Ensure notificationId is properly managed

        // Use the context to create notifications
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Critical Hit at " + timeStamp)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.applogo);

        // Send the notification to the user
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(uniqueNotificationId, builder.build());
    }


}
