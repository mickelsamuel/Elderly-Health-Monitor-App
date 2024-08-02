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
    private String caretakerID;
    private String caretakerName;
    private String caretakerPhoneNumber;
    private String role;
    private String lastName;          // New field
    private String emergencyContact;  // New field
    private String medicalCard;       // New field
    private String firstName;         // New field
    private String password;          // New field
    private String phoneNumber;       // New field
    private String id;                // New field

    public Patient() {
        // Default constructor required for calls to DataSnapshot.getValue(Patient.class)
    }

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
        this.caretakerID = "";
        this.caretakerName = "";
        this.caretakerPhoneNumber = "";
        this.role = "user";
        this.lastName = "";
        this.emergencyContact = "";
        this.medicalCard = "";
        this.firstName = "";
        this.password = "";
        this.phoneNumber = "";
        this.id = "";
    }

    // Getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return parseAccelerometerValue(0);
    }

    public double getAccelerometerY() {
        return parseAccelerometerValue(1);
    }

    public double getAccelerometerZ() {
        return parseAccelerometerValue(2);
    }

    private double parseAccelerometerValue(int index) {
        if (accelerometerReading == null || accelerometerReading.isEmpty()) {
            return 0.0;
        }
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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
}
