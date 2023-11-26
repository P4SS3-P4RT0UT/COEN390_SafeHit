package com.example.coen390_safehit.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.model.Player;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

public class SettingsActivity extends AppCompatActivity {

    // Person id to identify user
    private String uid;
    private String type;
    // Toolbar for back navigation
    private Toolbar toolbar;

    private Button scan;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Setup the toolbar
        setupToolBar();
        // Fetch the id from the previous activity
        uid = getIntent().getStringExtra("pid");
        type= getIntent().getStringExtra("type");
        scan = findViewById(R.id.scan);


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachDeviceClicked();
            }
        });


    }

    public void onLogOutClicked(View view) {
        goToSignIn();
    }

    public void onDeleteAccountClicked(View view) {
        // Delete account from database
    }
    public void onAttachDeviceClicked(){
//        Intent intent  = new Intent(getApplicationContext(), ScanningActivity.class);
//        startActivity(intent);
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this);

        scanner
                .startScan()
                .addOnSuccessListener(
                        barcode -> {
                            // Task completed successfully
                            String rawValue = barcode.getRawValue();
                            Player player = new Player();
                            player.setMac(rawValue);
                            Toast.makeText(SettingsActivity.this, "The device is connected", Toast.LENGTH_SHORT).show();

                        })
                .addOnCanceledListener(
                        () -> {
                            // Task canceled
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            Log.d("SETTINGS EXCEPTION", "Error ");
                            Toast.makeText(SettingsActivity.this,"Scanning did not work", Toast.LENGTH_SHORT).show();
                        });
    }
    public void onUpdateInfoClicked(View view) {
        goToPersonalInformation();
    }

    private void setupToolBar() {
        toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setTitle("Settings");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Redirect user back to their main page
        toolbar.setNavigationOnClickListener(view -> goBackToMainPage(type, uid));
    }

    public void goToSignIn() {
        Intent intent = new Intent(getApplicationContext(), SignIn.class);
        startActivity(intent);
    }

    public void goToPersonalInformation() {
        Intent intent = new Intent(getApplicationContext(), UpdateInformationActivity.class);
        intent.putExtra("pid", uid);
        intent.putExtra("type", type);
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
