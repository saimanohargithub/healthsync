package com.example.healthsync.frontend.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.healthsync.R;
import com.example.healthsync.frontend.ui.fragments.OnboardingPageFragment;

public class OnboardingViewPagerAdapter extends FragmentStateAdapter {

    private final int[] layouts = new int[]{
            R.layout.fragment_onboarding1,
            R.layout.fragment_onboarding2,
            R.layout.fragment_onboarding3
    };

    public OnboardingViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OnboardingPageFragment.newInstance(layouts[position]);
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }
}
