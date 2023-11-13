package com.example.coen390_safehit.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.coen390_safehit.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PlayerDataOverviewActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private PieChart pieChart;
    private LineChart lineChart, lineChart2;
    private HashMap<String, Integer> hitCount = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_data_overview);

        toolbar = findViewById(R.id.player_data_toolbar);
        pieChart = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);
        lineChart2 = findViewById(R.id.line_chart2);

        DatabaseReference directionRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("Time")
                .child("Direction");

        // Read the data once
        directionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);
                        hitValue = hitValue.replaceAll("Hit from ", "");

                        if (hitValue != null) {
                            hitCount.put(hitValue, hitCount.getOrDefault(hitValue, 0) + 1);
                        }
                    }

                    createBarGraph();

                } else {
                    // Handle the case where the 'Hard hit' node does not exist or is empty
                    Log.d("FirebaseData", "No direction hits recorded");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.w("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });

        DatabaseReference hardHit = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("Time")
                .child("hit")
                .child("Hard hit");
        hardHit.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Prepare the date format and calendar instances
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy@HH:mm:ss", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    long currentTime = System.currentTimeMillis();

                    // Time calculations
                    long oneWeekMillis = 7L * 24 * 60 * 60 * 1000;
                    long threeMonthsMillis = 3L * 30 * 24 * 60 * 60 * 1000; // Approximation of 3 months
                    long threeMonthsAgo = currentTime - threeMonthsMillis;
                    long oneWeekAgo = currentTime - oneWeekMillis;

                    // Data structures to hold the hit data
                    ArrayList<Entry> lastWeekEntries = new ArrayList<>();
                    TreeMap<Long, Integer> hitCountsPerWeek = new TreeMap<>();

                    int hitCount = 0;
                    // Process each hit
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);

                        if (hitValue != null) {
                            try {
                                String[] parts = hitValue.split("\\|");
                                Date date = dateFormat.parse(parts[0]);
                                float gForce = Float.parseFloat(parts[1]);

                                if (date != null) {
                                    long hitTime = date.getTime();

                                    // For the last week graph
                                    if (hitTime > oneWeekAgo) {
                                        hitCount++;
                                        // Convert date to hours and minutes for the X-axis value
                                        calendar.setTime(date);
                                        float time = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0f;
                                        lastWeekEntries.add(new Entry(time, hitCount)); // Add to last week's data
                                    }

                                    // For the 3 months graph
                                    if (hitTime > threeMonthsAgo) {
                                        // Find which week this hit belongs to
                                        long weeksSinceStart = (hitTime - threeMonthsAgo) / oneWeekMillis;
                                        long weekStartMillis = threeMonthsAgo + (weeksSinceStart * oneWeekMillis);

                                        // Increment the hit count for the corresponding week
                                        hitCountsPerWeek.put(weekStartMillis, hitCountsPerWeek.getOrDefault(weekStartMillis, 0) + 1);
                                    }
                                }
                            } catch (ParseException | NumberFormatException e) {
                                Log.d("FirebaseData", "Error parsing hit value: " + hitValue);
                            }
                        }
                    }

                    // Create graph for the last week's data
                    createLastWeekGraph(lastWeekEntries);

                    // Convert hit counts per week to entries and create the 3 months graph
                    ArrayList<Entry> threeMonthsEntries = new ArrayList<>();
                    for (Map.Entry<Long, Integer> entry : hitCountsPerWeek.entrySet()) {
                        threeMonthsEntries.add(new Entry(entry.getKey(), entry.getValue()));
                    }
                    createSeasonGraph(threeMonthsEntries);

                } else {
                    // Handle the case where the data does not exist or is empty
                    Log.d("FirebaseData", "No data recorded");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.w("FirebaseData", "Database error: " + databaseError.getMessage());
            }
        });

        setupToolBar();
    }

    void setupToolBar() {
        toolbar.setTitle(PlayerProfileActivity.playerName.getText());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(view -> finish());
    }

    void createBarGraph() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String key : hitCount.keySet()) {
            entries.add(new PieEntry(hitCount.get(key), key));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#1F77B4"));
        colors.add(Color.parseColor("#FF7F0E"));
        colors.add(Color.parseColor("#2CA02C"));
        colors.add(Color.parseColor("#D62728"));
        colors.add(Color.parseColor("#9467BD"));
        colors.add(Color.parseColor("#17BECF"));
        colors.add(Color.parseColor("#E377C2"));
        colors.add(Color.parseColor("#7F7F7F"));
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawEntryLabels(true);
        pieChart.setDescription(null);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setExtraOffsets(15, 15, 15, 15);


        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);


        pieChart.invalidate();
    }

    public void createLastWeekGraph(ArrayList<Entry> entries) {
        Collections.sort(entries, new EntryXComparator());
        LineDataSet lineDataSet = new LineDataSet(entries, "Hard hit rate over Time");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawValues(false);


        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setDescription(null);


        // Customize the X-axis to show hours and minutes
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat decimalFormat = new DecimalFormat("00");

            @Override
            public String getFormattedValue(float value) {
                int hours = (int) value;
                int minutes = (int) ((value - hours) * 60);
                return decimalFormat.format(hours) + ":" + decimalFormat.format(minutes);
            }
        });

        lineChart.invalidate(); // Refresh the chart
    }

    void createSeasonGraph(ArrayList<Entry> entries) {
        // Sort the entries by the timestamp
        Collections.sort(entries, new EntryXComparator());

        // Create the data set and set its properties
        LineDataSet lineDataSet = new LineDataSet(entries, "Hard hit rate over Time");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawValues(false);  // Don't draw values on the chart

        // Create the LineData object with the dataset
        LineData lineData = new LineData(lineDataSet);
        lineChart2.setData(lineData);
        lineChart2.getDescription().setEnabled(false);  // Disable the description

        // Customize the X-axis to show dates or week numbers
        XAxis xAxis = lineChart2.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                // Assuming the value is a timestamp in milliseconds
                long millis = (long) value;
                return dateFormat.format(new Date(millis));  // Format it as a date
            }
        });

        xAxis.setGranularity(1f); // Only show one date label per interval to avoid clutter
        xAxis.setLabelRotationAngle(-45); // Rotate labels to fit and be readable

        // Refresh the chart
        lineChart2.setExtraOffsets(0, 15, 0, 0);
        lineChart2.invalidate();
    }


}
