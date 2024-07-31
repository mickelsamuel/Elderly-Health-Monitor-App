package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextPassword;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPassword = findViewById(R.id.editTextPassword);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");

        findViewById(R.id.buttonCreateAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AccountCreationActivity.class));
            }
        });
    }

    public void onLoginClick(View view) {
        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter first name, last name, and password", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("firstName").equalTo(firstName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean loginSuccess = false;
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String dbLastName = userSnapshot.child("lastName").getValue(String.class);
                            String dbPassword = userSnapshot.child("password").getValue(String.class);
                            if (dbLastName != null && dbLastName.equals(lastName) && dbPassword != null && dbPassword.equals(password)) {
                                handleSuccessfulLogin(userSnapshot);
                                loginSuccess = true;
                                break;
                            }
                        }

                        if (!loginSuccess) {
                            Toast.makeText(LoginActivity.this, "Invalid name or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error", databaseError.toException());
                    }
                });
    }

    private void handleSuccessfulLogin(DataSnapshot userSnapshot) {
        String role = userSnapshot.child("role").getValue(String.class);
        String firstName = userSnapshot.child("firstName").getValue(String.class);
        String lastName = userSnapshot.child("lastName").getValue(String.class);
        String userId = userSnapshot.getKey();

        if ("caretaker".equals(role)) {
            startCaretakerActivity(firstName, lastName, userId);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Toast.makeText(this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCaretakerActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        startActivity(intent);
        finish();
    }

    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
