package com.example.healthsync.frontend.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.healthsync.frontend.ui.fragments.WizardActivityFragment;
import com.example.healthsync.frontend.ui.fragments.WizardBasicFragment;
import com.example.healthsync.frontend.ui.fragments.WizardGenderFragment;
import com.example.healthsync.frontend.ui.fragments.WizardGoalsFragment;
import com.example.healthsync.frontend.ui.fragments.WizardHabitsFragment;
import com.example.healthsync.frontend.ui.fragments.WizardMedicalFragment;
import com.example.healthsync.frontend.ui.fragments.WizardMetricsFragment;

public class WizardViewPagerAdapter extends FragmentStateAdapter {

    public WizardViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new WizardBasicFragment();
            case 1: return new WizardGenderFragment();
            case 2: return new WizardMetricsFragment();
            case 3: return new WizardGoalsFragment();
            case 4: return new WizardActivityFragment();
            case 5: return new WizardHabitsFragment();
            case 6: return new WizardMedicalFragment();
            default: return new WizardBasicFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
