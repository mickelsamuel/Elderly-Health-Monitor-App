package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CreateCaretakerActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextLicense;
    private Button buttonCreateCaretaker;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;
    private DatabaseReference validCaretakerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_caretaker);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = findViewById(R.id.editTextMedicalCard);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextLicense = findViewById(R.id.editTextLicense);
        buttonCreateCaretaker = findViewById(R.id.buttonCreateCaretaker);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");
        validCaretakerRef = firebaseDatabase.getReference("validCaretaker");

        buttonCreateCaretaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLicenseAndCreateCaretaker();
            }
        });
    }

    private void validateLicenseAndCreateCaretaker() {
        String license = editTextLicense.getText().toString().trim();

        if (TextUtils.isEmpty(license)) {
            Toast.makeText(CreateCaretakerActivity.this, "Please enter your license", Toast.LENGTH_SHORT).show();
            return;
        }

        validCaretakerRef.orderByChild("license").equalTo(license).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    createCaretakerAccount();
                } else {
                    Toast.makeText(CreateCaretakerActivity.this, "Invalid license", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateCaretakerActivity.this, "Error checking license", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCaretakerAccount() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String medicalCard = editTextMedicalCard.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String license = editTextLicense.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(medicalCard) || TextUtils.isEmpty(password) || TextUtils.isEmpty(license)) {
            Toast.makeText(CreateCaretakerActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String caretakerId = usersRef.push().getKey();

        // Initialize patientIDs as an empty list
        List<String> patientIDs = new ArrayList<>();

        User caretaker = new User(caretakerId, firstName, lastName, phoneNumber, medicalCard, password, "caretaker", license, patientIDs);

        if (caretakerId != null) {
            Log.d("Firebase", "Creating caretaker account with ID: " + caretakerId);
            Log.d("Firebase", "Caretaker data: " + caretaker.toString());
            usersRef.child(caretakerId).setValue(caretaker)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Caretaker account created successfully");
                        Toast.makeText(CreateCaretakerActivity.this, "Caretaker account created successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateCaretakerActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to create caretaker account", e);
                        Toast.makeText(CreateCaretakerActivity.this, "Failed to create caretaker account", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static class User {
        public String id;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        public String medicalCard;
        public String password;
        public String role;
        public String license;
        public List<String> patientIDs;

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String license, List<String> patientIDs) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.medicalCard = medicalCard;
            this.password = password;
            this.role = role;
            this.license = license;
            this.patientIDs = patientIDs;
        }
    }
}
