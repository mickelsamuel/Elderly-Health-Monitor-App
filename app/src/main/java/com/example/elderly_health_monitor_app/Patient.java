package com.example.elderly_health_monitor_app;

public class Patient {
    private String name;
    private String dob;
    private String patientID;
    private float temperature;
    private int heartRate;
    private String gender;
    private int age;
    private String lastVisitDate;
    private String accelerometerReading;

    public Patient(String name, String dob, String patientID) {
        this.name = name;
        this.dob = dob;
        this.patientID = patientID;
        this.temperature = 0.0f;
        this.heartRate = 0;
        this.gender = "";
        this.age = 0;
        this.lastVisitDate = "";
        this.accelerometerReading = "";
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public String getAccelerometerReading() {
        return accelerometerReading;
    }

    public void setAccelerometerReading(String accelerometerReading) {
        this.accelerometerReading = accelerometerReading;
    }

    public double getAccelerometerX() {
        return parseAccelerometerValue(0);
    }

    public double getAccelerometerY() {
        return parseAccelerometerValue(1);
    }

    public double getAccelerometerZ() {
        return parseAccelerometerValue(2);
    }

    private double parseAccelerometerValue(int index) {
        String[] values = accelerometerReading.split(",");
        if (values.length > index) {
            try {
                return Double.parseDouble(values[index].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0.0;
    }
}
