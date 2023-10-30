package com.example.coen390_safehit.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coen390_safehit.model.Database;
import com.example.coen390_safehit.R;

public class PlayerProfileActivity extends AppCompatActivity {

    TextView coachSuggestion, playerStatus, playerName;
    Database db = Database.getInstance(this);
    String playerID = Database.personID;


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
        loadPlayerName();
        loadStatus();
        loadSuggestion();
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

