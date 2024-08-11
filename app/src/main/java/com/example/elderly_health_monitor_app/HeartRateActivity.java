package com.example.elderly_health_monitor_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class HeartRateActivity extends AppCompatActivity {
    private static final String TAG = "HeartRateActivity";

    // UI components
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> heartRateList;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;

    // Firebase database references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_page);

        // Find the back button and other UI components
        ImageButton backButton = findViewById(R.id.backButton);
        listView = findViewById(R.id.listView);
        graph = findViewById(R.id.graph);

        // Set up the ListView and adapter
        heartRateList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, heartRateList);
        listView.setAdapter(adapter);

        // Set up the graph
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // Only 3 because of space

        // Set manual x bounds to have nice steps
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
        graph.getViewport().setMinX(sevenDaysAgo);
        graph.getViewport().setMaxX(now);
        graph.getViewport().setXAxisBoundsManual(true);

        // Enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        // Set OnClickListener to handle back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and go back to the previous one
                finish();
            }
        });

        // Initialize Firebase Database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        heartRateRef = firebaseDatabase.getReference("heartRateValues");

        // Read heart rate values from the database
        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                heartRateList.clear();
                ArrayList<DataPoint> dataPoints = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long heartVal = snapshot.child("heartVal").getValue(Long.class);
                    Long heartTime = snapshot.child("heartTime").getValue(Long.class);

                    Log.d(TAG, "Fetched data - HeartVal: " + heartVal + ", HeartTime: " + heartTime);

                    if (heartVal != null && heartTime != null) {
                        if (heartTime >= sevenDaysAgo && heartTime <= now) {
                            Log.d(TAG, "Valid data for last 7 days - HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                            addDataToList(heartVal, heartTime);
                            dataPoints.add(new DataPoint(new Date(heartTime), heartVal));
                        } else {
                            Log.d(TAG, "Data not within last 7 days - HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                        }
                    } else {
                        Log.e(TAG, "Invalid data - HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                    }
                }

                // Reverse the order of heartRateList to show the newest data first
                Collections.reverse(heartRateList);
                adapter.notifyDataSetChanged();

                // Sort the data points by timestamp
                Collections.sort(dataPoints, new Comparator<DataPoint>() {
                    @Override
                    public int compare(DataPoint dp1, DataPoint dp2) {
                        return Double.compare(dp1.getX(), dp2.getX());
                    }
                });

                // Add sorted data points to the series
                series.resetData(dataPoints.toArray(new DataPoint[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Log.e(TAG, "Error fetching heart rate data", databaseError.toException());
            }
        });
    }

    /**
     * Add heart rate data to the list view and update the adapter
     * @param heartVal The heart rate value
     * @param heartTime The timestamp of the heart rate value
     */
    private void addDataToList(final Long heartVal, final Long heartTime) {
        String formattedTime = convertTimestampToReadableDate(heartTime);
        String displayText = String.format("Heart Rate: %d bpm, Time: %s", heartVal, formattedTime);
        heartRateList.add(displayText);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Data added to list: " + displayText);
    }

    /**
     * Convert timestamp to a readable date format
     * @param timestamp The timestamp to convert
     * @return The formatted date string
     */
    private String convertTimestampToReadableDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
