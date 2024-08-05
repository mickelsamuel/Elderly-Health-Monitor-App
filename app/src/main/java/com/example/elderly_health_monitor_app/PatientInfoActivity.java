package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private TextView patientNameTextView, patientIDTextView, ageTextView, dobTextView, phoneNumberTextView, emergencyContactTextView, genderTextView, medicalCardTextView, lastVisitDateTextView;
    private TextView heartRateTextView, temperatureTextView, accelerometerTextView;
    private View heartRateStatus, temperatureStatus, accelerometerStatus;
    private CardView heartRateCard, temperatureCard, accelerometerCard;
    private DatabaseReference patientRef;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

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

        patientId = getIntent().getStringExtra("patientId");

        if (patientId != null) {
            patientRef = FirebaseDatabase.getInstance().getReference("users").child(patientId);
            loadPatientDetails();
        } else {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void setupListeners() {
        heartRateCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, HeartRateActivity.class)));
        temperatureCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, TemperatureActivity.class)));
        accelerometerCard.setOnClickListener(v -> startActivity(new Intent(PatientInfoActivity.this, AccelerometerActivity.class)));
    }

    private void loadPatientDetails() {
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    Integer age = dataSnapshot.child("age").getValue(Integer.class);
                    String dob = dataSnapshot.child("dob").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String emergencyContact = dataSnapshot.child("emergencyContact").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String medicalCard = dataSnapshot.child("medicalCard").getValue(String.class);
                    String lastVisitDate = dataSnapshot.child("lastVisitDate").getValue(String.class);

                    patientNameTextView.setText(firstName + " " + lastName);
                    patientIDTextView.setText(patientId);
                    ageTextView.setText("Age: " + (age != null ? age.toString() : "N/A"));
                    dobTextView.setText("DOB: " + dob);
                    phoneNumberTextView.setText("Phone: " + phoneNumber);
                    emergencyContactTextView.setText("Emergency Contact: " + emergencyContact);
                    genderTextView.setText("Gender: " + gender);
                    medicalCardTextView.setText("Medical Card: " + medicalCard);
                    lastVisitDateTextView.setText("Last Visit Date: " + lastVisitDate);

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

    private void setHeartRateIndicator(int heartRate) {
        heartRateTextView.setText("Heart Rate: " + heartRate + " bpm");
        if (heartRate < 60 || heartRate > 100) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_red);
        } else if (heartRate >= 60 && heartRate <= 100) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_yellow);
        }
    }

    private void setTemperatureIndicator(float temperature) {
        temperatureTextView.setText("Temperature: " + temperature + "°C");
        if (temperature < 36.5 || temperature > 37.5) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_red);
        } else if (temperature >= 36.5 && temperature <= 37.5) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_yellow);
        }
    }

    private void setAccelerometerIndicator(float x, float y, float z) {
        accelerometerTextView.setText("Accelerometer: X: " + x + ", Y: " + y + ", Z: " + z);
        if (Math.abs(x) > 1.0 || Math.abs(y) > 1.0 || Math.abs(z) > 10.0) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_red);
        } else if (Math.abs(x) <= 1.0 && Math.abs(y) <= 1.0 && Math.abs(z) <= 10.0) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_green);
        } else {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_yellow);
        }
    }
}
