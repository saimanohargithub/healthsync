package com.example.healthsync.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthsync.R;
import com.example.healthsync.databinding.ActivityOnboardingBinding;
import com.example.healthsync.frontend.ui.adapters.OnboardingAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<OnboardingAdapter.OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingAdapter.OnboardingItem(getString(R.string.ob_title_1), getString(R.string.ob_desc_1), R.raw.health_pulse));
        items.add(new OnboardingAdapter.OnboardingItem(getString(R.string.ob_title_2), getString(R.string.ob_desc_2), R.raw.health_pulse));
        items.add(new OnboardingAdapter.OnboardingItem(getString(R.string.ob_title_3), getString(R.string.ob_desc_3), R.raw.health_pulse));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabIndicator, binding.viewPager, (tab, position) -> {}).attach();

        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() < items.size() - 1) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
