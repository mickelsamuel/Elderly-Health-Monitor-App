package com.example.elderly_health_monitor_app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AccountCreationPagerAdapter extends FragmentStateAdapter {

    // Constructor that takes a FragmentActivity
    public AccountCreationPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    // Create a fragment based on the position in the ViewPager
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            // Return a new instance of PatientFragment for the first tab
            return new PatientFragment();
        } else {
            // Return a new instance of CaretakerFragment for the second tab
            return new CaretakerFragment();
        }
    }

    // Return the total number of tabs
    @Override
    public int getItemCount() {
        return 2;
    }
}
