package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import static com.example.elderly_health_monitor_app.LoginActivity.pn;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientInfoActivity extends AppCompatActivity {

    // UI elements to display patient information
    private TextView patientNameTextView, patientIDTextView, ageTextView, dobTextView, phoneNumberTextView, emergencyContactTextView, genderTextView, medicalCardTextView, lastVisitDateTextView;
    private TextView heartRateTextView, temperatureTextView, accelerometerTextView;
    private View heartRateStatus, temperatureStatus, accelerometerStatus;
    private CardView heartRateCard, temperatureCard, accelerometerCard;
    private Button backButton;

    // Firebase database reference for the patient
    private DatabaseReference patientRef;
    private String patientId;
    private String caretakerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        // Initialize UI elements
        patientNameTextView = findViewById(R.id.patientNameTextView);
        patientIDTextView = findViewById(R.id.patientIDTextView);
        ageTextView = findViewById(R.id.ageTextView);
        dobTextView = findViewById(R.id.dobTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        emergencyContactTextView = findViewById(R.id.emergencyContactTextView);
        genderTextView = findViewById(R.id.genderTextView);
        medicalCardTextView = findViewById(R.id.medicalCardTextView);
        lastVisitDateTextView = findViewById(R.id.lastVisitDateTextView);
        heartRateTextView = findViewById(R.id.heartRateTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        accelerometerTextView = findViewById(R.id.accelerometerTextView);

        heartRateStatus = findViewById(R.id.heartRateStatus);
        temperatureStatus = findViewById(R.id.temperatureStatus);
        accelerometerStatus = findViewById(R.id.accelerometerStatus);

        heartRateCard = findViewById(R.id.heartRateCard);
        temperatureCard = findViewById(R.id.temperatureCard);
        accelerometerCard = findViewById(R.id.accelerometerCard);
        backButton = findViewById(R.id.backButton);

        // Retrieve patient ID from intent
        patientId = getIntent().getStringExtra("patientId");
        caretakerId = getIntent().getStringExtra("caretakerId");

        // Check if patient ID is provided
        if (patientId != null) {
            patientRef = FirebaseDatabase.getInstance().getReference("users").child(patientId);
            loadPatientDetails();
        } else {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
        setupFirebaseListeners();
    }

    // Set up click listeners for the cards
    private void setupListeners() {
        heartRateCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, HeartRateActivity.class)));
        temperatureCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, TemperatureActivity.class)));
        accelerometerCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, AccelerometerActivity.class)));
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientInfoActivity.this, CaretakerMonitorActivity.class);
            intent.putExtra("caretakerId", caretakerId); // Ensure caretakerId is passed back
            startActivity(intent);
            finish(); // Optional: finish this activity to remove it from the back stack
        });
    }

    // Load patient details from Firebase
    private void loadPatientDetails() {
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve patient data from the snapshot
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    Long ageLong = dataSnapshot.child("age").getValue(Long.class);
                    Integer age = ageLong != null ? ageLong.intValue() : null;
                    String dob = dataSnapshot.child("dob").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String emergencyContact = dataSnapshot.child("emergencyContact").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String medicalCard = dataSnapshot.child("medicalCard").getValue(String.class);
                    String lastVisitDate = dataSnapshot.child("lastVisitDate").getValue(String.class);

                    // Set patient details to the TextViews
                    patientNameTextView.setText(firstName + " " + lastName);
                    patientIDTextView.setText(patientId);
                    ageTextView.setText("Age: " + (age != null ? age.toString() : "N/A"));
                    dobTextView.setText("DOB: " + dob);
                    phoneNumberTextView.setText("Phone: " + phoneNumber);
                    emergencyContactTextView.setText("Emergency Contact: " + emergencyContact);
                    genderTextView.setText("Gender: " + gender);
                    medicalCardTextView.setText("Medical Card: " + medicalCard);
                    lastVisitDateTextView.setText("Last Visit Date: " + lastVisitDate);

                    // Set dummy values for indicators
                    setHeartRateIndicator(75); // Dummy heart rate value
                    setTemperatureIndicator(37.0f); // Dummy temperature value
                    setAccelerometerIndicator(0.1f, 0.2f, 9.8f); // Dummy accelerometer values
                } else {
                    Toast.makeText(PatientInfoActivity.this, "Patient data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PatientInfoActivity.this, "Failed to load patient data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set heart rate indicator based on value
    private void setHeartRateIndicator(int heartRate) {
        heartRateTextView.setText("Heart Rate: " + heartRate + " bpm");
        if (heartRate < 60 || heartRate > 100) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_red);
            sendNotification(patientNameTextView.getText() + " HEART RATE CRITICAL", "Heart Rate: " + heartRate);
        } else if (heartRate >= 60 && heartRate <= 100) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification(patientNameTextView.getText() + " HEART RATE WARNING", "Heart Rate: " + heartRate);
        }
    }

    // Set temperature indicator based on value
    private void setTemperatureIndicator(float temperature) {
        temperatureTextView.setText("Temperature: " + temperature + "°C");
        if (temperature < 36.5 || temperature > 37.5) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_red);
            sendNotification(patientNameTextView.getText() + " TEMPERATURE CRITICAL", "Temperature: " + temperature);
        } else if (temperature >= 36.5 && temperature <= 37.5) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification(patientNameTextView.getText() + " TEMPERATURE WARNING", "Temperature: " + temperature);
        }
    }

    // Set accelerometer indicator based on values
    private void setAccelerometerIndicator(float x, float y, float z) {
        accelerometerTextView.setText("Accelerometer: X: " + x + ", Y: " + y + ", Z: " + z);
        if (Math.abs(x) > 1.0 || Math.abs(y) > 1.0 || Math.abs(z) > 10.0) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_red);
            sendNotification(patientNameTextView.getText() + " FALL DETECTED", "");
        } else if (Math.abs(x) <= 1.0 && Math.abs(y) <= 1.0 && Math.abs(z) <= 10.0) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification(patientNameTextView.getText() + " FALL POSSIBLE", "");

        }
    }

    /**
     * Set up Firebase listeners for real-time updates
     */
    private void setupFirebaseListeners() {
        DatabaseReference sharedDataRef = FirebaseDatabase.getInstance().getReference("sharedPatientData").child(patientId);

        sharedDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double heartRate = dataSnapshot.child("heartRate").getValue(Double.class);
                    Double temperature = dataSnapshot.child("temperature").getValue(Double.class);
                    Double accelerometerX = dataSnapshot.child("accelerometerX").getValue(Double.class);
                    Double accelerometerY = dataSnapshot.child("accelerometerY").getValue(Double.class);
                    Double accelerometerZ = dataSnapshot.child("accelerometerZ").getValue(Double.class);

                    if (heartRate != null) {
                        setHeartRateIndicator(heartRate.intValue());
                    }
                    if (temperature != null) {
                        setTemperatureIndicator(temperature.floatValue());
                    }
                    if (accelerometerX != null && accelerometerY != null && accelerometerZ != null) {
                        setAccelerometerIndicator(accelerometerX.floatValue(), accelerometerY.floatValue(), accelerometerZ.floatValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PatientInfoActivity.this, "Failed to load shared data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String title, String message) {
        pn.sendNotification(this, title, message);
    }
}
//EOF