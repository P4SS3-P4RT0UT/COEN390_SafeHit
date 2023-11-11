package com.example.coen390_safehit.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;

public class PlayerProfileActivity extends AppCompatActivity {

    TextView coachSuggestion, playerStatus, playerName;
    DatabaseHelper db = DatabaseHelper.getInstance(this);
    String playerID = DatabaseHelper.personID;

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
        setupToolbar();
        loadPlayerName();
        loadStatus();
        loadSuggestion();
    }

    void setupToolbar() {
        // Settings icon
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(view -> goToSettings());
    }

    void goToSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("pid", playerID);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadPlayerName() {
        db.getPlayerNameFromPlayerID(playerID);
    }

    private void loadStatus() {
        db.getStatus(playerID);
    }

    private void loadSuggestion() {
        db.getSuggestion(playerID);
    }


}

