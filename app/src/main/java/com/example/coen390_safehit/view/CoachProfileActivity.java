package com.example.coen390_safehit.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.PlayerAdapter;
import com.example.coen390_safehit.model.Coach;
import com.example.coen390_safehit.model.Player;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoachProfileActivity extends AppCompatActivity {
    // Layout elements
    private Toolbar toolbar;
    private ListView playerListView;
    private Spinner teamSpinner;
    // Settings icon
    ImageButton btnSettings;

    // Database
    DatabaseHelper db;
    String coachID;

    private List<String> teamsList = new ArrayList<>();
    ArrayAdapter<String> teamAdapter;
    private String currentTeamName = null;
    public String currentTeamID = null;
    private List<Player> playerslist = new ArrayList<>();
    PlayerAdapter playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_profile);
        db = DatabaseHelper.getInstance(this);
        coachID = DatabaseHelper.personID;
        toolbar = findViewById(R.id.toolbar);
        teamSpinner = findViewById(R.id.teamList);
        playerListView = findViewById(R.id.playerList);

        loadTeams();
        setupTeamSpinner();
        setupToolBar();
        setupPlayerList();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupPlayerList() {
        playerAdapter = new PlayerAdapter(this, playerslist);
        playerListView.setAdapter(playerAdapter);
        playerListView.setOnItemClickListener((adapterView, view, position, l) -> {
            selectedProfile = (Player) adapterView.getItemAtPosition(position);

            if (selectedProfile.getMac() == null) {
                Toast.makeText(this, "Player does not have a device connected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CoachDataOverviewActivity.class);
            intent.putExtra("mac", selectedProfile.getMac());
            startActivity(intent);
        });
    }

    private void loadPlayers() {
        playerslist.clear();
        db.playerDocumentList.clear();
        db.getPlayersFromTeamID(currentTeamID, new DatabaseHelper.FetchCallback() {
            @Override
            public void onComplete() {
                for (DocumentSnapshot player : db.playerDocumentList) {
                    db.getPersonFromPlayerID(player.getString("PID"), task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot person = task.getResult();
                            if (person.exists()) {
                                Player p = new Player(person.getString("Email"), person.getString("FirstName"), person.getString("LastName"), player.getString("PID"), Integer.parseInt(player.getString("Number")), player.getString("Position"), person.getString("Team"), player.getString("Status"), player.getString("Suggestion"), player.getString("mac"));
                                Log.d("Player", person.getString("FirstName") + " " + person.getString("LastName") + ", " + player.getString("Position") + ", " + player.getString("Number"));
                                String playerText = person.getString("FirstName") + " " + person.getString("LastName") + ", " + player.getString("Position") + ", " + player.getString("Number");
                                playerslist.add(p);
                                playerHashMap.put(playerText, p);
                                playerAdapter.notifyDataSetChanged();
                            } else {
                                // Handle the error
                            }
                        } else {
                            // Handle the error
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CoachProfileActivity.this, "Error loading players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeams() {
        teamsList.clear();
        db.getTeamsFromCoachID(coachID, new DatabaseHelper.FetchCallback() {
            @Override
            public void onComplete() {
                db.teamsList.forEach((key, value) -> {
                    if (currentTeamID == null) {
                        currentTeamName = key;
                        currentTeamID = value;
                    }
                    teamsList.add(key);
                });

                teamAdapter.notifyDataSetChanged();
                DatabaseHelper.currentTeamID = db.teamsList.get(currentTeamName);
                DatabaseHelper.currentTeamName = currentTeamName;
                loadPlayers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CoachProfileActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static HashMap<String, Player> playerHashMap = new HashMap<>();
    public static Player selectedProfile;


    private boolean isFirstLoad = true;

    private void setupTeamSpinner() {
        teamAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_coach, teamsList);
        teamAdapter.setDropDownViewResource(R.layout.spinner_item_coach);
        teamSpinner.setAdapter(teamAdapter);


        //Once teamSpinner changes run switchTeams()
        teamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }
                switchTeams();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    private void switchTeams() {
        currentTeamName = teamSpinner.getSelectedItem().toString();
        currentTeamID = db.teamsList.get(currentTeamName);
        loadPlayers();
    }

    private void setupToolBar() {
        toolbar.setTitle("");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
        // Settings icon
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(view -> goToSettings());
    }

    void goToSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("pid", coachID);
        intent.putExtra("type", "Coach");
        startActivity(intent);
    }


}
