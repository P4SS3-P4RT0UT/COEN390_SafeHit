package com.example.coen390_safehit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CoachProfileActivity extends AppCompatActivity {
    // Layout elements
    private Toolbar toolbar;
    private ListView playerList;
    private AppCompatSpinner teamSpinner;


    // Database
    Database db = Database.getInstance();
    String coachID = Database.personID;
    private List<String> teamsList = new ArrayList<>();
    private String currentTeamName = null;
    public String currentTeamID = null;
    private List<String> playerslist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_profile);

        toolbar = findViewById(R.id.toolbar);
        teamSpinner = findViewById(R.id.teamList);
        playerList = findViewById(R.id.playerList);

        loadTeams();
        loadPlayers();
        setupToolBar();
        setupPlayerList();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupPlayerList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerslist);
        playerList.setAdapter(adapter);


    }

    private void loadPlayers() {
        db.getPlayersFromTeamID(currentTeamID);
        db.playerList.forEach((key, value) -> {
            playerslist.add(value);
        });
    }

    private void loadTeams() {
        db.getTeamsFromCoachID(coachID);
        db.teamsList.forEach((key, value) -> {
            if (currentTeamID == null) {
                currentTeamID = key;
                currentTeamName = value;
            }
            teamsList.add(value);
        });
    }

    private void switchTeams() {
        currentTeamName = teamSpinner.getSelectedItem().toString();
        currentTeamID = db.teamsList.get(currentTeamName);

        playerslist.clear();
        loadPlayers();
        setupPlayerList();
    }

    private void setupToolBar() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, teamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        toolbar.setTitle("");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
    }


}
