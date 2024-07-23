package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private Button monitorButton;
    private Button caretakerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        monitorButton = findViewById(R.id.monitorButton);
        caretakerButton = findViewById(R.id.caretakerButton);

        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent monitorIntent = new Intent(MainActivity.this, MonitorActivity.class);
                startActivity(monitorIntent);
            }
        });

        caretakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent caretakerIntent = new Intent(MainActivity.this, CaretakerMonitorActivity.class);
                startActivity(caretakerIntent);
            }
        });
    }
}
