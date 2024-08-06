package com.example.elderly_health_monitor_app;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.content.Context;

public class TemperatureMonitor {
    private static final double DEFAULT_FEVER_THRESHOLD = 37.5;
    private static final double DEFAULT_HYPOTHERMIA_THRESHOLD = 35.0;

    private final double feverThreshold;
    private final double hypothermiaThreshold;
    private final List<Reading> readings = new ArrayList<>();
    private Context context;
    private Patient patient;

    public TemperatureMonitor(Context context, Patient patient) {
        this(DEFAULT_FEVER_THRESHOLD, DEFAULT_HYPOTHERMIA_THRESHOLD, context, patient);
    }

    public TemperatureMonitor(double feverThreshold, double hypothermiaThreshold, Context context, Patient patient) {
        this.feverThreshold = feverThreshold;
        this.hypothermiaThreshold = hypothermiaThreshold;
        this.context = context;
        this.patient = patient;
    }

    public void addReading(double temperature, long timestamp) {
        readings.add(new Reading(temperature, timestamp));
        checkImmediateAlert(temperature);
        checkTrendAlert();
    }

    private void checkImmediateAlert(double temperature) {
        if (temperature > feverThreshold) {
            triggerAlert("High fever detected for " + patient.getFirstName() + " " + patient.getLastName());
        } else if (temperature < hypothermiaThreshold) {
            triggerAlert("Hypothermia detected for " + patient.getFirstName() + " " + patient.getLastName());
        }
    }

    private void checkTrendAlert() {
        if (readings.size() < 48) {
            return;
        }

        double rollingAvg = calculateRollingAverage(24);
        if (rollingAvg > feverThreshold) {
            triggerAlert("Sustained high temperature trend detected for " + patient.getFirstName() + " " + patient.getLastName());
        } else if (rollingAvg < hypothermiaThreshold) {
            triggerAlert("Sustained low temperature trend detected for " + patient.getFirstName() + " " + patient.getLastName());
        }
    }

    private double calculateRollingAverage(int period) {
        int start = readings.size() - period;
        List<Reading> recentReadings = readings.subList(start, readings.size());
        double sum = 0;
        for (Reading reading : recentReadings) {
            sum += reading.temperature;
        }
        return sum / period;
    }

    private void triggerAlert(String message) {
        System.out.println("ALERT: " + message);

        Intent alertIntent = new Intent("com.example.elderly_health_monitor_app.TEMPERATURE_ALERT");
        alertIntent.putExtra("alertMessage", message);
        alertIntent.putExtra("patientName", patient.getFirstName() + " " + patient.getLastName());
        context.sendBroadcast(alertIntent);
    }

    private static class Reading {
        double temperature;
        long timestamp;

        Reading(double temperature, long timestamp) {
            this.temperature = temperature;
            this.timestamp = timestamp;
        }
    }
}
