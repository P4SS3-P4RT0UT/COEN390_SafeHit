package com.example.coen390_safehit.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.controller.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
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

import org.w3c.dom.Text;

import java.lang.reflect.Array;
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
    private TextView hitTypeTextView;

    String playerID;

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;

    ArrayList<String> hitCountArrayList = new ArrayList<>();
    ArrayList<String> lastWeekHitArraylist = new ArrayList();
    ArrayList<String> seasonHitArrayList = new ArrayList<>();
    float threshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_data_overview);

        playerID = getIntent().getStringExtra("pid");


        toolbar = findViewById(R.id.player_data_toolbar);
        pieChart = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);
        lineChart2 = findViewById(R.id.line_chart2);
        nestedScrollView = findViewById(R.id.nested_scroll_view);

        hitTypeTextView = findViewById(R.id.hit_textView);

        progressBar = findViewById(R.id.progressBar);
        threshold = Float.parseFloat(DatabaseHelper.threshold);
        getHitData();
        setupToolBar();
    }

    void getHitData() {
        DatabaseReference hitRef = FirebaseDatabase.getInstance("https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("08:D1:F9:A4:F7:38")
                .child("hit");

        hitRef.addListenerForSingleValueEvent(new ValueEventListener() {
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


                    // Process each hit
                    for (DataSnapshot hitSnapshot : dataSnapshot.getChildren()) {
                        String hitValue = hitSnapshot.getValue(String.class);

                        if (hitValue != null) {
                            try {
                                String[] parts = hitValue.split("\\|");
                                Date date = dateFormat.parse(parts[0]);
                                float gForce = Float.parseFloat(parts[1]);
                                String direction = parts[2];

                                direction = direction.replaceAll("Hit from ", "");
                                hitCountArrayList.add(direction + "|" + gForce);

                                if (date != null) {
                                    long hitTime = date.getTime();

                                    // For the last week graph
                                    if (hitTime > oneWeekAgo) {
                                        // Convert date to a float representing minutes since midnight
                                        calendar.setTime(date);
                                        float time = calendar.get(Calendar.HOUR_OF_DAY) * 60.0f + calendar.get(Calendar.MINUTE);
                                        lastWeekHitArraylist.add(time + "|" + gForce);
                                    }

                                    // For the 3 months graph
                                    if (hitTime > threeMonthsAgo) {
                                        // Find which week this hit belongs to
                                        long weeksSinceStart = (hitTime - threeMonthsAgo) / oneWeekMillis;
                                        long weekStartMillis = threeMonthsAgo + (weeksSinceStart * oneWeekMillis);

                                        // Increment the hit count for the corresponding week
                                        seasonHitArrayList.add(weekStartMillis + "|" + gForce);
                                    }
                                }
                            } catch (ParseException | NumberFormatException e) {
                                Log.d("FirebaseData", "Error parsing hit value: " + hitValue);
                            }
                        }
                    }

                    createDirectionGraph(4, 8, "Hard hit");
                    createSeasonGraph(4, 8, "Hard hit");
                    createLastWeekGraph(4, 8, "Hard hit");
                    progressBar.setVisibility(View.GONE);
                    toolbar.setVisibility(View.VISIBLE);
                    nestedScrollView.setVisibility(View.VISIBLE);

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
    }

    void setupToolBar() {
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        String fn = getIntent().getStringExtra("fn");
        title.setText(fn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CoordinatorLayout coordinatorLayout = findViewById(R.id.background);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.soft_hit) {
                coordinatorLayout.setBackgroundTintList(null);
                hitTypeTextView.setText("Soft Hit");
                createDirectionGraph(0, 4, "Soft hit");
                createSeasonGraph(0, 4, "Soft hit");
                createLastWeekGraph(0, 4, "Soft hit");
                return true;
            } else if (item.getItemId() == R.id.hard_hit) {
                //backgroundtint in yellow
                coordinatorLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7A3F00")));
                hitTypeTextView.setText("Hard Hit");
                createDirectionGraph(4, threshold, "Hard hit");
                createSeasonGraph(4, threshold, "Hard hit");
                createLastWeekGraph(4, threshold, "Hard hit");
                return true;
            } else if (item.getItemId() == R.id.critical_hit) {
                coordinatorLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7A0000")));
                hitTypeTextView.setText("Critical Hit");
                createDirectionGraph(threshold, 100, "Critical hit");
                createSeasonGraph(threshold, 100, "Critical hit");
                createLastWeekGraph(threshold, 100, "Critical hit");
                return true;
            }
            return false;
        });


        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_data_overview_menu, menu);
        return true;
    }

    void createDirectionGraph(float threshold1, float threshold2, String hitStrength) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        HashMap<String, Float> directionTotals = new HashMap<>();

        for (String key : hitCountArrayList) {
            String[] hit = key.split("\\|");
            float force = Float.parseFloat(hit[1]);
            String direction = hit[0];

            if (force > threshold1 && force < threshold2)
                directionTotals.put(direction, directionTotals.getOrDefault(direction, 0f) + 1);
        }

        for (Map.Entry<String, Float> entry : directionTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
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
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Assuming you want to show two decimal places
                return String.format(Locale.ENGLISH, "%.0f", value);
            }
        });

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

    public void createLastWeekGraph(float threshold1, float threshold2, String hitStrength) {
        ArrayList<Entry> entries = new ArrayList<>();
        HashMap<Float, Integer> lastWeekHitMap = new HashMap<>();

        int hitCount = 0;
        for (String key : lastWeekHitArraylist) {
            String[] hit = key.split("\\|");
            float force = Float.parseFloat(hit[1]);
            float time = Float.parseFloat(hit[0]);  // Time in minutes since midnight

            if (force > threshold1 && force < threshold2) {
                lastWeekHitMap.put(time, hitCount++);
            }
        }

        for (Map.Entry<Float, Integer> entry : lastWeekHitMap.entrySet()) {
            entries.add(new Entry(entry.getKey(), entry.getValue()));  // Use Entry, not PieEntry
        }


        entries.sort(new EntryXComparator());
        LineDataSet lineDataSet = new LineDataSet(entries, hitStrength + " rate over time");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawValues(false);


        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setDescription(null);


        // Customize the X-axis to show hours and minutes
        XAxis xAxis = lineChart.getXAxis();
        DecimalFormat decimalFormat = new DecimalFormat("00");
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int totalMinutes = (int) value;
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                return decimalFormat.format(hours) + ":" + decimalFormat.format(minutes);
            }
        });

        lineChart.invalidate(); // Refresh the chart
    }

    void createSeasonGraph(float threshold1, float threshold2, String hitStrength) {
        ArrayList<Entry> entries = new ArrayList<>();

        TreeMap<Long, Integer> seasonHitTreeMap = new TreeMap<>();

        for (String key : seasonHitArrayList) {
            String[] hit = key.split("\\|");
            float force = Float.parseFloat(hit[1]);
            long time = Long.parseLong(hit[0]);

            if (force > threshold1 && force < threshold2) {
                seasonHitTreeMap.put(time, seasonHitTreeMap.getOrDefault(time, 0) + 1);
            }
        }

        for (Map.Entry<Long, Integer> entry : seasonHitTreeMap.entrySet()) {

            entries.add(new Entry(entry.getKey(), entry.getValue()));
        }


        // Sort the entries by the timestamp
        Collections.sort(entries, new EntryXComparator());

        // Create the data set and set its properties
        LineDataSet lineDataSet = new LineDataSet(entries, hitStrength + " rate over Time");
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
                long millis = (long) value;
                String formattedDate = dateFormat.format(new Date(millis));
                Log.d("ChartDate", "Timestamp: " + millis + ", Date: " + formattedDate);
                return formattedDate;
            }
        });

        xAxis.setGranularity(1f); // Only show one date label per interval to avoid clutter
        xAxis.setLabelRotationAngle(-45); // Rotate labels to fit and be readable

        // Refresh the chart
        lineChart2.setExtraOffsets(0, 15, 0, 0);
        lineChart2.invalidate();
    }


}
