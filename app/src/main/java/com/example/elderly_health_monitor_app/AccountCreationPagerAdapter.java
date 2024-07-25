package com.example.elderly_health_monitor_app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AccountCreationPagerAdapter extends FragmentStateAdapter {

    public AccountCreationPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PatientFragment();
        } else {
            return new CaretakerFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
