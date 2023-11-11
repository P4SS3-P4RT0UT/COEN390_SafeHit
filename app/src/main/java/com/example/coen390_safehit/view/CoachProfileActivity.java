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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CoachProfileActivity extends AppCompatActivity {
    // Layout elements
    private Toolbar toolbar;
    private ListView playerList;
    private Spinner teamSpinner;
    // Settings icon
    ImageButton btnSettings;

    // Database
    DatabaseHelper db = DatabaseHelper.getInstance(this);
    String coachID = DatabaseHelper.personID;

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
        setupTeamSpinner();
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
        playerList.setOnItemClickListener((adapterView, view, i, l) -> {
//            String selectedItem = (String) adapterView.getItemAtPosition(position);
//            selectedProfile = studentMap.get(selectedItem);

            Intent intent = new Intent(this, CoachDataOverviewActivity.class);
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
                                Log.d("Player", person.getString("FirstName") + " " + person.getString("LastName") + ", " + player.getString("Position") + ", " + player.getString("Number"));
                                playerslist.add(person.getString("FirstName") + " " + person.getString("LastName") + ", " + player.getString("Position") + ", " + player.getString("Number"));
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
                loadPlayers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CoachProfileActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isFirstLoad = true;

    private void setupTeamSpinner() {
        teamAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, teamsList);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    //TODO: Switch teams
    private void switchTeams() {
        currentTeamName = teamSpinner.getSelectedItem().toString();
        currentTeamID = db.teamsList.get(currentTeamName);
        loadPlayers();
    }

    private void setupToolBar() {
        toolbar.setTitle("Coach Profile");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
        // Settings icon
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(view -> goToSettings());
    }

    void goToSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("pid", coachID);
        startActivity(intent);
    }


}
