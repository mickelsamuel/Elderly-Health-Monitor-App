package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBarFontSize;
    private TextView textFontSize;
    private TextView textFontSizeLabel;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        seekBarFontSize = findViewById(R.id.seekBar_font_size);
        textFontSize = findViewById(R.id.text_font_size);
        textFontSizeLabel = findViewById(R.id.text_font_size_label);
        buttonLogout = findViewById(R.id.button_logout);

        // Set the current font size from shared preferences
        float currentFontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        textFontSize.setTextSize(currentFontSize);
        textFontSizeLabel.setTextSize(currentFontSize);
        buttonLogout.setTextSize(currentFontSize);
        seekBarFontSize.setProgress((int) currentFontSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textFontSize.setTextSize(progress);
                textFontSizeLabel.setTextSize(progress);
                buttonLogout.setTextSize(progress);
                getSharedPreferences("settings", MODE_PRIVATE).edit().putFloat("font_size", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        buttonLogout.setOnClickListener(v -> {
            // Handle logout action here
            // TODO: Implement logout functionality
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the back button click here
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
