package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CaretakerMonitorActivity extends AppCompatActivity {

    private TextView userNameText;
    private TextView statusSummary;
    private Button addPatientButton;
    private Spinner sortSpinner;

    private ArrayList<Patient> patients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker_monitor);

        userNameText = findViewById(R.id.userNameText);
        statusSummary = findViewById(R.id.statusSummary);
        addPatientButton = findViewById(R.id.addPatientButton);
        sortSpinner = findViewById(R.id.sortSpinner);

        // Setup Spinner for sorting
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        addPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaretakerMonitorActivity.this, AddPatientActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Handle sorting selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortPatients(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // TODO: Load existing patients from database or other storage
    }

    private void sortPatients(int position) {
        switch (position) {
            case 0: // Sort by name
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return p1.getName().compareTo(p2.getName());
                    }
                });
                break;
            case 1: // Sort by patient ID
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return p1.getPatientID().compareTo(p2.getPatientID());
                    }
                });
                break;
            case 2: // Sort by date of birth
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return p1.getDob().compareTo(p2.getDob());
                    }
                });
                break;
            case 3: // Sort by oxygen level
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return Integer.compare(p1.getOxygenLevel(), p2.getOxygenLevel());
                    }
                });
                break;
            case 4: // Sort by temperature
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return Float.compare(p1.getTemperature(), p2.getTemperature());
                    }
                });
                break;
            case 5: // Sort by heart rate
                Collections.sort(patients, new Comparator<Patient>() {
                    @Override
                    public int compare(Patient p1, Patient p2) {
                        return Integer.compare(p1.getHeartRate(), p2.getHeartRate());
                    }
                });
                break;
            // Add more sorting criteria as needed
        }
        // Update UI with sorted patients
        updatePatientCards();
    }

    private void updatePatientCards() {
        // TODO: Update the UI to display sorted patient cards
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Handle new patient data from AddPatientActivity
            String name = data.getStringExtra("name");
            String dob = data.getStringExtra("dob");
            String patientID = data.getStringExtra("patientID");

            Patient newPatient = new Patient(name, dob, patientID);
            patients.add(newPatient);
            updatePatientCards();
        }
    }
}
