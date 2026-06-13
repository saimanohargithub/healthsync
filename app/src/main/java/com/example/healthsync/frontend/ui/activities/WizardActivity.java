package com.example.healthsync.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.healthsync.R;
import com.example.healthsync.databinding.ActivityWizardBinding;
import com.example.healthsync.frontend.firebase.FirestoreManager;
import com.example.healthsync.frontend.ui.adapters.WizardViewPagerAdapter;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.frontend.utils.UserDataHolder;
import com.google.firebase.auth.FirebaseAuth;

public class WizardActivity extends AppCompatActivity {

    private ActivityWizardBinding binding;
    private int totalSteps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WizardViewPagerAdapter adapter =
                new WizardViewPagerAdapter(this);

        totalSteps = adapter.getItemCount();

        binding.wizardPager.setAdapter(adapter);
        binding.wizardPager.setUserInputEnabled(false);

        binding.btnContinue.setOnClickListener(v -> {

            if (binding.wizardPager.getCurrentItem()
                    < totalSteps - 1) {

                binding.wizardPager.setCurrentItem(
                        binding.wizardPager.getCurrentItem() + 1
                );

            } else {

                completeSetup();
            }
        });

        binding.wizardPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {

                        binding.wizardProgress.setProgress(
                                ((position + 1) * 100) / totalSteps,
                                true
                        );

                        if (position == totalSteps - 1) {

                            binding.btnContinue.setText(
                                    R.string.btn_finalize
                            );

                        } else {

                            binding.btnContinue.setText(
                                    R.string.btn_continue
                            );
                        }
                    }
                }
        );
    }

    private void completeSetup() {

        FirebaseAuth auth =
                FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            String uid =
                    auth.getCurrentUser().getUid();

            if (UserDataHolder.userProfile.name == null || UserDataHolder.userProfile.name.trim().isEmpty()) {
                UserDataHolder.userProfile.name = "User";
            }
            if (UserDataHolder.userProfile.age <= 0) UserDataHolder.userProfile.age = 25;
            if (UserDataHolder.userProfile.height <= 0) UserDataHolder.userProfile.height = 170.0f;
            if (UserDataHolder.userProfile.weight <= 0) UserDataHolder.userProfile.weight = 70.0f;
            if (UserDataHolder.userProfile.waterGoal <= 0) UserDataHolder.userProfile.waterGoal = 2500.0f;

            FirestoreManager.saveUser(
                    uid,
                    UserDataHolder.userProfile
            );
        }

        PreferenceManager prefs =
                new PreferenceManager(this);

        // Save user profile locally

        prefs.setUserName(
                UserDataHolder.userProfile.name
        );

        prefs.setUserAge(
                UserDataHolder.userProfile.age
        );

        prefs.setUserGender(
                UserDataHolder.userProfile.gender
        );

        prefs.setUserHeight(
                UserDataHolder.userProfile.height
        );

        prefs.setUserWeight(
                UserDataHolder.userProfile.weight
        );

        // Save goals

        prefs.setWaterGoal(
                (int) UserDataHolder.userProfile.waterGoal
        );

        // Mark setup complete

        prefs.setSetupComplete(true);

        Intent intent =
                new Intent(
                        WizardActivity.this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        finish();
    }
}
