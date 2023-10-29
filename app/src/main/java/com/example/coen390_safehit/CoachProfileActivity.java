package com.example.coen390_safehit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CoachProfileActivity extends AppCompatActivity {
    // Layout elements
    private Toolbar toolbar;
    private ListView playerList;
    private Spinner teamSpinner;


    // Database
    Database db = Database.getInstance(this);
    String coachID = Database.personID;

    private List<String> teamsList = new ArrayList<>();
    ArrayAdapter<String> teamAdapter;
    private String currentTeamName = null;
    public String currentTeamID = null;
    private List<String> playerslist = new ArrayList<>();
    ArrayAdapter<String> playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_profile);

        toolbar = findViewById(R.id.toolbar);
        teamSpinner = findViewById(R.id.teamList);
        playerList = findViewById(R.id.playerList);

        loadTeams();
        setupToolBar();
        setupPlayerList();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupPlayerList() {
        playerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerslist);
        playerList.setAdapter(playerAdapter);
    }

    private void loadPlayers() {
        db.getPlayersFromTeamID(currentTeamID, new Database.FetchCallback() {
            @Override
            public void onComplete() {
                for (DocumentSnapshot player : db.playerDocumentList) {
                    db.getPersonFromPlayerID(player.get("PID").toString(), task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot person = task.getResult();
                            if (person.exists()) {
                                playerslist.add(person.get("FirstName") + " " + person.get("LastName") + ", " + player.get("Position") + ", " + player.get("Number"));
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
        db.getTeamsFromCoachID(coachID, new Database.FetchCallback() {
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
                loadPlayers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CoachProfileActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //TODO: Switch teams
    private void switchTeams() {
        currentTeamName = teamSpinner.getSelectedItem().toString();
        currentTeamID = db.teamsList.get(currentTeamName);

        playerslist.clear();
        loadPlayers();
        setupPlayerList();
    }

    private void setupToolBar() {
        teamAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, teamsList);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(teamAdapter);

        toolbar.setTitle("Coach Profile");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
    }


}
