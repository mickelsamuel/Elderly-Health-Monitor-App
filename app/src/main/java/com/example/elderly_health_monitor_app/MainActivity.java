package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createUserButton;
    private Button createCaretakerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        loginButton = findViewById(R.id.loginButton);
        createUserButton = findViewById(R.id.createUserButton);
        createCaretakerButton = findViewById(R.id.createCaretakerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createUserIntent = new Intent(MainActivity.this, CreateUserActivity.class);
                startActivity(createUserIntent);
            }
        });

        createCaretakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createCaretakerIntent = new Intent(MainActivity.this, CreateCaretakerActivity.class);
                startActivity(createCaretakerIntent);
            }
        });
    }
}
