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

public class AccelerometerActivity extends AppCompatActivity {
    private static final String TAG = "AccelerometerActivity";

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> accelerometerList;
    private GraphView graph;
    private LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference accelerometerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_page);

        // Initialize the back button and list view from the layout
        ImageButton backButton = findViewById(R.id.backButton);
        listView = findViewById(R.id.listView);
        graph = findViewById(R.id.graph);

        // Set up the ListView and its adapter
        accelerometerList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accelerometerList);
        listView.setAdapter(adapter);

        // Initialize the graph series for X, Y, and Z axes
        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();
        setupGraph(graph);

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
        accelerometerRef = firebaseDatabase.getReference("accelerometerValues");

        // Read accelerometer values from the database
        accelerometerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accelerometerList.clear();
                ArrayList<DataPoint> dataPointsX = new ArrayList<>();
                ArrayList<DataPoint> dataPointsY = new ArrayList<>();
                ArrayList<DataPoint> dataPointsZ = new ArrayList<>();
                long now = System.currentTimeMillis();
                long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;

                // Loop through all the children of the data snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve accelerometer values and timestamp
                    Double accelerometerXVal = snapshot.child("accelerometerXVal").getValue(Double.class);
                    Double accelerometerYVal = snapshot.child("accelerometerYVal").getValue(Double.class);
                    Double accelerometerZVal = snapshot.child("accelerometerZVal").getValue(Double.class);
                    Long accelerometerTime = snapshot.child("accelerometerTime").getValue(Long.class);

                    Log.d(TAG, "Fetched data - X: " + accelerometerXVal + ", Y: " + accelerometerYVal + ", Z: " + accelerometerZVal + ", Time: " + accelerometerTime);

                    // Check if values are not null and within the last 7 days
                    if (accelerometerXVal != null && accelerometerYVal != null && accelerometerZVal != null && accelerometerTime != null) {
                        if (accelerometerTime >= sevenDaysAgo && accelerometerTime <= now) {
                            Log.d(TAG, "Valid data for last 7 days - X: " + accelerometerXVal + ", Y: " + accelerometerYVal + ", Z: " + accelerometerZVal + ", Time: " + accelerometerTime);
                            addDataToList(accelerometerXVal, accelerometerYVal, accelerometerZVal, accelerometerTime);
                            dataPointsX.add(new DataPoint(new Date(accelerometerTime), accelerometerXVal));
                            dataPointsY.add(new DataPoint(new Date(accelerometerTime), accelerometerYVal));
                            dataPointsZ.add(new DataPoint(new Date(accelerometerTime), accelerometerZVal));
                        } else {
                            Log.d(TAG, "Data not within last 7 days - X: " + accelerometerXVal + ", Y: " + accelerometerYVal + ", Z: " + accelerometerZVal + ", Time: " + accelerometerTime);
                        }
                    } else {
                        Log.e(TAG, "Invalid data - X: " + accelerometerXVal + ", Y: " + accelerometerYVal + ", Z: " + accelerometerZVal + ", Time: " + accelerometerTime);
                    }
                }

                // Sort the data points by timestamp
                sortDataPointsByTimestamp(dataPointsX);
                sortDataPointsByTimestamp(dataPointsY);
                sortDataPointsByTimestamp(dataPointsZ);

                // Add sorted data points to the series
                seriesX.resetData(dataPointsX.toArray(new DataPoint[0]));
                seriesY.resetData(dataPointsY.toArray(new DataPoint[0]));
                seriesZ.resetData(dataPointsZ.toArray(new DataPoint[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.e(TAG, "Error fetching accelerometer data", databaseError.toException());
            }
        });
    }

    /**
     * Set up the graph with necessary properties and series
     *
     * @param graph The GraphView object to set up
     */
    private void setupGraph(GraphView graph) {
        graph.addSeries(seriesX);
        graph.addSeries(seriesY);
        graph.addSeries(seriesZ);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of space

        // Set manual x bounds to have nice steps
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
        graph.getViewport().setMinX(sevenDaysAgo);
        graph.getViewport().setMaxX(now);
        graph.getViewport().setXAxisBoundsManual(true);

        // Enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        // Set colors for different series
        seriesX.setColor(android.graphics.Color.RED);
        seriesY.setColor(android.graphics.Color.GREEN);
        seriesZ.setColor(android.graphics.Color.BLUE);
    }

    /**
     * Sort the data points by timestamp in ascending order
     *
     * @param dataPoints The list of data points to sort
     */
    private void sortDataPointsByTimestamp(ArrayList<DataPoint> dataPoints) {
        Collections.sort(dataPoints, new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint dp1, DataPoint dp2) {
                return Double.compare(dp1.getX(), dp2.getX());
            }
        });
    }

    /**
     * Add accelerometer data to the list view and notify the adapter
     *
     * @param accelerometerXVal The X-axis value of the accelerometer
     * @param accelerometerYVal The Y-axis value of the accelerometer
     * @param accelerometerZVal The Z-axis value of the accelerometer
     * @param accelerometerTime The timestamp of the accelerometer data
     */
    private void addDataToList(final Double accelerometerXVal, final Double accelerometerYVal, final Double accelerometerZVal, final Long accelerometerTime) {
        String formattedTime = convertTimestampToReadableDate(accelerometerTime);
        String displayText = String.format("X: %.2f m/s², Y: %.2f m/s², Z: %.2f m/s², Time: %s", accelerometerXVal, accelerometerYVal, accelerometerZVal, formattedTime);
        accelerometerList.add(displayText);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Data added to list: " + displayText);
    }

    /**
     * Convert a timestamp to a readable date string
     *
     * @param timestamp The timestamp to convert
     * @return A formatted date string
     */
    private String convertTimestampToReadableDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
