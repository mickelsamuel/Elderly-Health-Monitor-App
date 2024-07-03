package com.example.elderly_health_monitor_app; // Replace with your actual package name

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MonitorActivity extends AppCompatActivity {

    private TextView oxygenReading;
    private TextView temperatureReading;
    private TextView accelerometerReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        // Initialize TextViews
        oxygenReading = findViewById(R.id.oxygenReading);
        temperatureReading = findViewById(R.id.temperatureReading);
        accelerometerReading = findViewById(R.id.accelerometerReading);

        // TODO: Implement real sensor data reading
        updateReadings(98, 36.5f, "X: 0.1, Y: 0.2, Z: 9.8");
    }

    private void updateReadings(int oxygen, float temperature, String accelerometer) {
        oxygenReading.setText("Oxygen: " + oxygen + "%");
        temperatureReading.setText("Temperature: " + temperature + "°C");
        accelerometerReading.setText("Accelerometer: " + accelerometer);
    }
}