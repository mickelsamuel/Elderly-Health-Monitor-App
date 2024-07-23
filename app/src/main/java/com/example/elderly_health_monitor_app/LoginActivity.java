package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText editTextPhoneNumber;
    private EditText editTextPassword;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");
    }

    public void onLoginClick(View view) {
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (phoneNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting login with phone number: " + phoneNumber);

        usersRef.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        String role = userSnapshot.child("role").getValue(String.class);
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String lastName = userSnapshot.child("lastName").getValue(String.class);
                        String caretakerId = userSnapshot.getKey();
                        String license = userSnapshot.child("license").getValue(String.class);

                        Log.d(TAG, "User found with role: " + role);

                        if (dbPassword != null && dbPassword.equals(password)) {
                            if ("caretaker".equals(role)) {
                                Intent intent = new Intent(LoginActivity.this, CaretakerMonitorActivity.class);
                                intent.putExtra("caretakerName", firstName + " " + lastName);
                                intent.putExtra("caretakerId", caretakerId);
                                intent.putExtra("caretakerLicense", license);
                                startActivity(intent);
                                finish();
                                Log.d(TAG, "Login successful, redirecting to CaretakerMonitorActivity");
                            } else if ("user".equals(role)) {
                                Intent intent = new Intent(LoginActivity.this, MonitorActivity.class);
                                intent.putExtra("userName", firstName + " " + lastName);
                                intent.putExtra("userId", caretakerId);
                                startActivity(intent);
                                finish();
                                Log.d(TAG, "Login successful, redirecting to MonitorActivity");
                            } else {
                                Toast.makeText(LoginActivity.this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Access denied: Invalid role");
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid phone number or password", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Invalid phone number or password");
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error", databaseError.toException());
            }
        });
    }
}
