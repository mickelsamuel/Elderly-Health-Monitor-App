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

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> heartRateList;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_page);

        // Find the back button
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
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of space

        // set manual x bounds to have nice steps
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;
        graph.getViewport().setMinX(sevenDaysAgo);
        graph.getViewport().setMaxX(now);
        graph.getViewport().setXAxisBoundsManual(true);

        // enable scaling and scrolling
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
        usersRef = firebaseDatabase.getReference("users");

        // Read heart rate values from the database
        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                heartRateList.clear();
                ArrayList<DataPoint> dataPoints = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.child("id").getValue(String.class);
                    Long heartVal = snapshot.child("heartVal").getValue(Long.class);
                    Long heartTime = snapshot.child("heartTime").getValue(Long.class);

                    Log.d(TAG, "Fetched data - ID: " + userId + ", HeartVal: " + heartVal + ", HeartTime: " + heartTime);

                    if (userId != null && heartVal != null && heartTime != null) {
                        if (heartTime >= sevenDaysAgo && heartTime <= now) {
                            Log.d(TAG, "Valid data for last 7 days - ID: " + userId + ", HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                            fetchUserNameAndAddToList(userId, heartVal, heartTime);
                            dataPoints.add(new DataPoint(new Date(heartTime), heartVal));
                        } else {
                            Log.d(TAG, "Data not within last 7 days - ID: " + userId + ", HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                        }
                    } else {
                        Log.e(TAG, "Invalid data - ID: " + userId + ", HeartVal: " + heartVal + ", HeartTime: " + heartTime);
                    }
                }

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
                // Handle possible errors.
                Log.e(TAG, "Error fetching heart rate data", databaseError.toException());
            }
        });
    }

    private void fetchUserNameAndAddToList(final String userId, final Long heartVal, final Long heartTime) {
        usersRef.orderByChild("id").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    for (DataSnapshot user : userSnapshot.getChildren()) {
                        String firstName = user.child("firstName").getValue(String.class);
                        String lastName = user.child("lastName").getValue(String.class);
                        if (firstName != null && lastName != null) {
                            String userName = firstName + " " + lastName;
                            String formattedTime = convertTimestampToReadableDate(heartTime);
                            String displayText = "Name: " + userName + ", Heart Value: " + heartVal + ", Time: " + formattedTime;
                            heartRateList.add(displayText);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "User found and added to list: " + displayText);
                        } else {
                            Log.e(TAG, "First name or last name is null for user ID: " + userId);
                        }
                    }
                } else {
                    Log.e(TAG, "No user found for ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.e(TAG, "Error fetching user data", databaseError.toException());
            }
        });
    }

    private String convertTimestampToReadableDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
