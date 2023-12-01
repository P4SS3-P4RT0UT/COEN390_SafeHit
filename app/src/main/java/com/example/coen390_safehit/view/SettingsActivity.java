package com.example.coen390_safehit.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.controller.QRCodeScanner;
import com.example.coen390_safehit.model.Coach;
import com.example.coen390_safehit.model.Player;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

public class SettingsActivity extends AppCompatActivity {

    // Person id to identify user
    private static String uid;
    private String type;
    private String team;
    // Toolbar for back navigation
    private Toolbar toolbar;

    private static Button scan;
    private Button backButton;
    private Button thresholdButton;
    private Button deleteButton;
    private SeekBar thresholdSeekBar;
    TextView thresholdValue, thresholdText;
    static DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        db = DatabaseHelper.getInstance(this);

        // Fetch the id from the previous activity
        uid = getIntent().getStringExtra("pid");
        type = getIntent().getStringExtra("type");
        team = getIntent().getStringExtra("teamName");

        scan = findViewById(R.id.scan);
        thresholdButton = findViewById(R.id.thresholdButton);
        thresholdSeekBar = findViewById(R.id.thresholdBar);
        thresholdValue = findViewById(R.id.thresholdValue);
        thresholdText = findViewById(R.id.textView3);


        boolean changed = false;

        if (type.equals("Coach")) {
            scan.setVisibility(View.GONE);
            thresholdButton.setVisibility(View.VISIBLE);
            thresholdValue.setVisibility(View.VISIBLE);
            thresholdText.setVisibility(View.VISIBLE);
            thresholdValue.setText(db.threshold);

            thresholdButton.setOnClickListener(v -> {
                db.udpateCoachThreshold(String.valueOf(thresholdSeekBar.getProgress() / 10.0f), uid);
                Toast.makeText(SettingsActivity.this, "Threshold updated", Toast.LENGTH_SHORT).show();
            });

            thresholdSeekBar.setVisibility(View.VISIBLE);

            thresholdSeekBar.setProgress((int) (Double.parseDouble(db.threshold) * 10));
            thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                float progressChangedValue = 0;

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    progressChangedValue = progress / 10.0f; // Divide by 10 to get the decimal value
                    thresholdValue = findViewById(R.id.thresholdValue);
                    thresholdValue.setText(String.format("%.1f", progressChangedValue)); // Display value with one decimal
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    thresholdValue = findViewById(R.id.thresholdValue);
                    thresholdValue.setText(String.format("%.1f", progressChangedValue)); // Display value with one decimal
                }
            });

        } else {
            thresholdButton.setVisibility(View.GONE);
            thresholdValue.setVisibility(View.GONE);
            thresholdSeekBar.setVisibility(View.GONE);
            thresholdText.setVisibility(View.GONE);

            scan.setVisibility(View.VISIBLE);
            if (DatabaseHelper.macAddress == null) {
                scan.setText("Connect to a Device");
                scan.setOnClickListener(v -> QRCodeScanner.onAttachDeviceClicked(uid, this));
            } else {
                scan.setText("Unlink Device");
                scan.setOnClickListener(v -> {
                    db.unlinkDevice(this, DatabaseHelper.personID);
                });
            }

        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> goBackToMainPage(type, uid));

        deleteButton = findViewById(R.id.DeleteAccount);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    public static void resetLinkButton(Context context) {
        if (DatabaseHelper.macAddress == null) {
            scan.setText("Connect to a Device");
            scan.setOnClickListener(v -> QRCodeScanner.onAttachDeviceClicked(uid, context));
        } else {
            scan.setText("Unlink Device");
            scan.setOnClickListener(v -> {
                db.unlinkDevice(context, DatabaseHelper.personID);
            });

        }
    }

    public void onLogOutClicked(View view) {
        goToSignIn();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? Your data will be lost permanently.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call the delete account method from DatabaseHelper
                DatabaseHelper.getInstance(SettingsActivity.this).deleteAccount(uid, type);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    public void onUpdateInfoClicked(View view) {
        goToPersonalInformation();
    }

    public void goToSignIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("remember", "false");
        editor.putString("email", "");
        editor.putString("password", "");
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }

    public void goToPersonalInformation() {
        Intent intent = new Intent(getApplicationContext(), UpdateInformationActivity.class);
        intent.putExtra("pid", uid);
        intent.putExtra("type", type);
        intent.putExtra("teamName", team);
        startActivity(intent);
    }

    public void goBackToMainPage(String type, String pid) {
        switch (type) {
            // Go back to coach main page
            case "Coach":
                Intent coachProfile = new Intent(getApplicationContext(), CoachProfileActivity.class);
                startActivity(coachProfile);
                break;
            // Go back to player main page
            case "Player":
                Intent playerProfile = new Intent(getApplicationContext(), PlayerProfileActivity.class);
                startActivity(playerProfile);
                break;
            case "Trainer":
                // Empty
                break;
            default:
                Log.d("SETTINGS EXCEPTION", "Error trying to go back to user main page");
        }
    }
}
