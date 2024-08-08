package com.example.elderly_health_monitor_app;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Patient {
    // Patient attributes
    private String firstName;
    private String lastName;
    private String dob; // Date of birth
    private String patientID;
    private float temperature;
    private int heartRate;
    private String gender;
    private int age;
    private String lastVisitDate;
    private String accelerometerReading;
    private String caretakerID;
    private String caretakerName;
    private String caretakerPhoneNumber;
    private String role;
    private String emergencyContact;
    private String medicalCard;
    private String password;
    private String phoneNumber;
    private String id;

    private double accelerometerX;
    private double accelerometerY;
    private double accelerometerZ;

    // Default constructor required for calls to DataSnapshot.getValue(Patient.class)
    public Patient() {
    }

    // Parameterized constructor
    public Patient(String firstName, String lastName, String dob, String patientID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.patientID = patientID;
        this.temperature = 0.0f;
        this.heartRate = 0;
        this.gender = "";
        this.age = 0;
        this.lastVisitDate = "";
        this.accelerometerReading = "";
        this.caretakerID = "";
        this.caretakerName = "";
        this.caretakerPhoneNumber = "";
        this.role = "user";
        this.emergencyContact = "";
        this.medicalCard = "";
        this.password = "";
        this.phoneNumber = "";
        this.id = patientID;

        this.accelerometerX = 0.0;
        this.accelerometerY = 0.0;
        this.accelerometerZ = 0.0;
    }

    // Getters and setters for all fields
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
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
        return accelerometerX;
    }

    public void setAccelerometerX(double accelerometerX) {
        this.accelerometerX = accelerometerX;
    }

    public double getAccelerometerY() {
        return accelerometerY;
    }

    public void setAccelerometerY(double accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public double getAccelerometerZ() {
        return accelerometerZ;
    }

    public void setAccelerometerZ(double accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public String getCaretakerID() {
        return caretakerID;
    }

    public void setCaretakerID(String caretakerID) {
        this.caretakerID = caretakerID;
    }

    public String getCaretakerName() {
        return caretakerName;
    }

    public void setCaretakerName(String caretakerName) {
        this.caretakerName = caretakerName;
    }

    public String getCaretakerPhoneNumber() {
        return caretakerPhoneNumber;
    }

    public void setCaretakerPhoneNumber(String caretakerPhoneNumber) {
        this.caretakerPhoneNumber = caretakerPhoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getMedicalCard() {
        return medicalCard;
    }

    public void setMedicalCard(String medicalCard) {
        this.medicalCard = medicalCard;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Method to fetch temperature data from Firebase
    public void fetchTemperatureData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("temperatureValues");
        databaseReference.orderByChild("id").equalTo(this.patientID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    temperature = snapshot.child("temperatureVal").getValue(Float.class);
                    // Process timestamp if needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Method to fetch heart rate data from Firebase
    public void fetchHeartRateData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("heartRateValues");
        databaseReference.orderByChild("id").equalTo(this.patientID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    heartRate = snapshot.child("heartVal").getValue(Integer.class);
                    // Process timestamp if needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    // Method to fetch accelerometer data from Firebase
    public void fetchAccelerometerData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("accelerometerValues");
        databaseReference.orderByChild("id").equalTo(this.patientID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    accelerometerX = snapshot.child("accelerometerXVal").getValue(Double.class);
                    accelerometerY = snapshot.child("accelerometerYVal").getValue(Double.class);
                    accelerometerZ = snapshot.child("accelerometerZVal").getValue(Double.class);
                    // Process timestamp if needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
