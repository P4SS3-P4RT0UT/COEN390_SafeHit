package com.example.coen390_safehit.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.coen390_safehit.controller.DatabaseHelper;
import com.example.coen390_safehit.model.Player;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.example.coen390_safehit.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class CoachDataOverviewActivity extends AppCompatActivity {
    int hardHitCount = 0;
    int softHitCount = 0;
    int criticalHitCount = 0;

    Player player;
    DatabaseHelper database;

    Button dataButton;


    TextView positionTextView, numberTextView, suggestionTextView, statusTextView;
    MenuItem editAction;

    ProgressBar progressBar;
    Toolbar toolbar;
    LinearLayout linearLayout;
    CardView cardViewPlayerStatus, cardViewPlayerSuggestion, cardViewPlayerData;


    String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_data_overview);

        database = DatabaseHelper.getInstance(this);

        macAddress = getIntent().getStringExtra("mac");

        // Get a reference to the 'Hard hit' node
        DatabaseReference hitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child(macAddress)
                .child("hit");

        // Read the data once
        hitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);


                        if (hitValue != null) {
                            String[] hit = hitValue.split("\\|");
                            if (Double.parseDouble(hit[1]) < 4) {
                                softHitCount++;
                            } else if (Double.parseDouble(hit[1]) < Double.parseDouble(DatabaseHelper.threshold)) {
                                hardHitCount++;
                            } else {
                                criticalHitCount++;
                            }

                            Log.d("FirebaseData", "Hard hit value: " + hitValue);
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    toolbar.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);
                    cardViewPlayerData.setVisibility(View.VISIBLE);
                    cardViewPlayerSuggestion.setVisibility(View.VISIBLE);
                    cardViewPlayerStatus.setVisibility(View.VISIBLE);
                    dataButton.setVisibility(View.VISIBLE);
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

    // vulnerable, concussed, cleared

    private void setupToolBar() {
        toolbar = findViewById(R.id.player_data_toolbar);
        toolbar.setTitle("");
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(player.getFirstName() + " " + player.getLastName());
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
            player.setSuggestion(suggestionTextView.getText().toString());
            player.setStatus(statusTextView.getText().toString());
            database.updatePlayerData(player);
        } else {
            suggestionTextView.setEnabled(true);
            statusTextView.setEnabled(true);
            editAction.setIcon(R.drawable.ic_check);
        }
    }

    void setupData() {
        player = CoachProfileActivity.selectedProfile;


        progressBar = findViewById(R.id.progressBar);
        positionTextView = findViewById(R.id.coach_data_position);
        numberTextView = findViewById(R.id.coach_data_number);
        suggestionTextView = findViewById(R.id.textViewDisplaySuggestion);
        statusTextView = findViewById(R.id.textViewDisplayStatus);

        dataButton = findViewById(R.id.dataButton2);

        dataButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PlayerDataOverviewActivity.class);
            intent.putExtra("pid", player.getPid());
            intent.putExtra("fn", player.getFirstName() + " " + player.getLastName());
            intent.putExtra("mac", player.getMac());

            startActivity(intent);
        });

        linearLayout = findViewById(R.id.linearLayout2);
        cardViewPlayerData = findViewById(R.id.cardViewPlayerData);
        cardViewPlayerSuggestion = findViewById(R.id.cardViewSuggestion);
        cardViewPlayerStatus = findViewById(R.id.cardViewPlayerStatus);

        positionTextView.setText(player.getPosition());
        numberTextView.setText(String.valueOf(player.getNumber()));
        suggestionTextView.setText(player.getSuggestion());
        statusTextView.setText(player.getStatus());

        suggestionTextView.setBackgroundResource(android.R.drawable.edit_text);
        statusTextView.setBackgroundResource(android.R.drawable.edit_text);


    }

    void createGraph() {
        PieChart pieChart = findViewById(R.id.pie_chart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(softHitCount, "Soft Hit"));
        entries.add(new PieEntry(hardHitCount, "Hard Hit"));
        entries.add(new PieEntry(criticalHitCount, "Critical Hit"));


        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(13f);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Assuming you want to show two decimal places
                return String.format(Locale.ENGLISH, "%.0f", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDescription(null);
        pieChart.setEntryLabelColor(R.color.black);

        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }
}
