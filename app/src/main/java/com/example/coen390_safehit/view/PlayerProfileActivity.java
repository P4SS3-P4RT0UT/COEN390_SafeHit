package com.example.coen390_safehit.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.QRCodeScanner;

public class PlayerProfileActivity extends AppCompatActivity {
    public static TextView playerName;

    TextView coachSuggestion, playerStatus;
    static Button dataButton;

    DatabaseHelper db = DatabaseHelper.getInstance(this);
    String playerID = DatabaseHelper.personID;

    String userType;


    // Settings icon
    ImageButton btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profile);
        playerName = findViewById(R.id.playerName);
        db.playerName = playerName;
        playerStatus = findViewById(R.id.textViewDisplayStatus);
        db.playerStatus = playerStatus;
        coachSuggestion = findViewById(R.id.textViewDisplaySuggestion);
        db.coachSuggestion = coachSuggestion;
        dataButton = findViewById(R.id.dataButton);

        setupToolbar();
        loadPlayerName();
        loadStatus();
        loadSuggestion();


        //loadType();
    }

    public static void updateButton(Context context) {
        if (DatabaseHelper.macAddress == null) {
            dataButton.setText("Connect a new device\n to view data");
            dataButton.setOnClickListener(view -> {
                QRCodeScanner.onAttachDeviceClicked(context);
            });
        } else {
            dataButton.setText("View Impacts Data");

            dataButton.setOnClickListener(view -> {
                Intent intent = new Intent(context, PlayerDataOverviewActivity.class);
                intent.putExtra("fn", playerName.getText().toString());
                context.startActivity(intent);
            });
        }
    }


    void setupToolbar() {
        // Settings icon
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(view -> goToSettings());
    }

    void goToSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("pid", playerID);
        intent.putExtra("type", "Player");

        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadPlayerName() {
        db.getPlayerNameFromPlayerID(playerID, this);

    }

    private void loadStatus() {
        db.getStatus(playerID);
    }

    private void loadSuggestion() {
        db.getSuggestion(playerID);
    }


}

