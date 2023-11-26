package com.example.coen390_safehit.view;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.model.Player;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.example.coen390_safehit.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CoachDataOverviewActivity extends AppCompatActivity {
    int hardHitCount = 0;
    int softHitCount = 0;

    Player player;
    DatabaseHelper database;

    TextView positionTextView, numberTextView, suggestionTextView, statusTextView;
    MenuItem editAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_data_overview);

        database = DatabaseHelper.getInstance(this);

        // Get a reference to the 'Hard hit' node
        DatabaseReference hitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("Time")
                .child("hit");

        DatabaseReference hardHitRef = hitRef.child("Hard hit");

        // Read the data once
        hardHitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);

                        if (hitValue != null) {
                            hardHitCount++;
                            Log.d("FirebaseData", "Hard hit value: " + hitValue);
                        }
                    }

                    createGraph();

                } else {
                    // Handle the case where the 'Hard hit' node does not exist or is empty
                    Log.d("FirebaseData", "No hard hits recorded");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.w("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });

        DatabaseReference softHitRef = hitRef.child("Soft hit");

        softHitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);

                        if (hitValue != null) {
                            softHitCount++;
                            Log.d("FirebaseData", "Hard hit value: " + hitValue);
                        }
                    }

                    createGraph();

                } else {
                    // Handle the case where the 'Hard hit' node does not exist or is empty
                    Log.d("FirebaseData", "No hard hits recorded");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.w("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });

        setupData();
        setupToolBar();

    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.player_data_toolbar);
        toolbar.setTitle(player.getFirstName() + " " + player.getLastName());
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit_action) {
                switchToEditMode();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coach_data_overview_menu, menu);
        editAction = menu.findItem(R.id.edit_action);
        return true;
    }

    void switchToEditMode() {
        if (suggestionTextView.isEnabled()) {
            //edit action change icon

            editAction.setIcon(R.drawable.ic_edit);
            suggestionTextView.setEnabled(false);
            statusTextView.setEnabled(false);
            database.updatePlayerData(player, suggestionTextView.getText().toString(), statusTextView.getText().toString());
        } else {
            suggestionTextView.setEnabled(true);
            statusTextView.setEnabled(true);
            editAction.setIcon(R.drawable.ic_check);
        }
    }

    void setupData() {
        player = CoachProfileActivity.selectedProfile;

        positionTextView = findViewById(R.id.coach_data_position);
        numberTextView = findViewById(R.id.coach_data_number);
        suggestionTextView = findViewById(R.id.textViewDisplaySuggestion);
        statusTextView = findViewById(R.id.textViewDisplayStatus);

        positionTextView.setText(player.getPosition());
        numberTextView.setText(String.valueOf(player.getNumber()));
        suggestionTextView.setText(player.getSuggestion());
        statusTextView.setText(player.getStatus());


    }

    void createGraph() {
        if (hardHitCount == 0 || softHitCount == 0) {
            return;
        }
        PieChart pieChart = findViewById(R.id.pie_chart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(hardHitCount, "Hard Hit"));
        entries.add(new PieEntry(softHitCount, "Soft Hit"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(13f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDescription(null);
        pieChart.setEntryLabelColor(R.color.black);

        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }
}
