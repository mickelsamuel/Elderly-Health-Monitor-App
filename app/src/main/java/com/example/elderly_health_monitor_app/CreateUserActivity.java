package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateUserActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextDob, editTextAge, editTextGender;
    private Button buttonCreateUser;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = findViewById(R.id.editTextMedicalCard);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextDob = findViewById(R.id.editTextDob);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        buttonCreateUser = findViewById(R.id.buttonCreateUser);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");

        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount();
            }
        });
    }

    private void createUserAccount() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String medicalCard = editTextMedicalCard.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(medicalCard) || TextUtils.isEmpty(password) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(age) || TextUtils.isEmpty(gender)) {
            Toast.makeText(CreateUserActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = usersRef.push().getKey();

        User user = new User(userId, firstName, lastName, phoneNumber, medicalCard, password, "user", dob, Integer.parseInt(age), gender);

        if (userId != null) {
            usersRef.child(userId).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateUserActivity.this, "User account created successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateUserActivity.this, "Failed to create user account", Toast.LENGTH_SHORT).show());
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
        public String dob;
        public int age;
        public String gender;

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String dob, int age, String gender) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.medicalCard = medicalCard;
            this.password = password;
            this.role = role;
            this.dob = dob;
            this.age = age;
            this.gender = gender;
        }
    }
}
