package com.example.elderly_health_monitor_app;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        // Enable Firebase disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
