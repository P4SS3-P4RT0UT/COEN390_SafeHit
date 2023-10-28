package com.example.coen390_safehit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.List;

public class CoachProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView playerList;
    private AppCompatSpinner teamList;
    private List<String> teamsList;
    private List<String> playerslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_profile);

        toolbar = findViewById(R.id.toolbar);
        teamList = findViewById(R.id.teamList);
        playerList = findViewById(R.id.playerList);

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

    }

    private void setupToolBar() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, teamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamList.setAdapter(adapter);

        toolbar.setTitle("");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
    }


}
