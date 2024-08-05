package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class AccountCreationActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AccountCreationPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        // Initialize the TabLayout and ViewPager2 from the layout
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Create an instance of the pager adapter
        pagerAdapter = new AccountCreationPagerAdapter(this);

        // Set the adapter to the ViewPager2
        viewPager.setAdapter(pagerAdapter);

        // Link the TabLayout and the ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Set the tab titles based on the position
                    if (position == 0) {
                        tab.setText("Patient");
                    } else {
                        tab.setText("Caretaker");
                    }
                }).attach(); // Attach the TabLayoutMediator to synchronize the TabLayout and ViewPager2
    }
}
