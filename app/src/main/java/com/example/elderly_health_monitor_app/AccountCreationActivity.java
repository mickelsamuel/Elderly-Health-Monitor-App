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

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new AccountCreationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Patient");
                    } else {
                        tab.setText("Caretaker");
                    }
                }).attach();
    }
}
