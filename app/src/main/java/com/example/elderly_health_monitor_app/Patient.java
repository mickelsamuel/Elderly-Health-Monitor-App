package com.example.elderly_health_monitor_app;

public class Patient {
    private String name;
    private String dob;
    private String patientID;
    private int oxygenLevel;
    private float temperature;
    private int heartRate;

    public Patient(String name, String dob, String patientID) {
        this.name = name;
        this.dob = dob;
        this.patientID = patientID;
        // Initialize additional fields as needed
        this.oxygenLevel = 0;
        this.temperature = 0.0f;
        this.heartRate = 0;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getPatientID() {
        return patientID;
    }

    public int getOxygenLevel() {
        return oxygenLevel;
    }

    public void setOxygenLevel(int oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
