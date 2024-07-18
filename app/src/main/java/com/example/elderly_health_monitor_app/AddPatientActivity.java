package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddPatientActivity extends AppCompatActivity {

    private EditText patientNameInput;
    private EditText patientDOBInput;
    private EditText patientIDInput;
    private Button savePatientButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientNameInput = findViewById(R.id.patientNameInput);
        patientDOBInput = findViewById(R.id.patientDOBInput);
        patientIDInput = findViewById(R.id.patientIDInput);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);

        savePatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = patientNameInput.getText().toString();
                String dob = patientDOBInput.getText().toString();
                String patientID = patientIDInput.getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("dob", dob);
                resultIntent.putExtra("patientID", patientID);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
