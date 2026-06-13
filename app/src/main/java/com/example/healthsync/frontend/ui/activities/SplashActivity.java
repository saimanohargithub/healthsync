package com.example.healthsync.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthsync.databinding.ActivitySplashBinding;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        com.google.firebase.appcheck.FirebaseAppCheck firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance());

        // Verify Firebase Connection
        if (FirebaseApp.getApps(this).size() > 0) {
            Log.d("FirebaseTest", "Firebase Connected Successfully");
        } else {
            Log.e("FirebaseTest", "Firebase Connection Failed");
        }

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Schedule Reminders
        new com.example.healthsync.frontend.utils.HealthNotificationManager(this).scheduleReminders();

        animateLogo();

        new Handler().postDelayed(() -> {

            PreferenceManager prefs = new PreferenceManager(this);

            Intent intent;

            if (FirebaseAuth.getInstance().getCurrentUser() != null && prefs.isSetupComplete()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );

            finish();

        }, SPLASH_DELAY);
    }

    private void animateLogo() {

        binding.ivLogo.setAlpha(0f);
        binding.ivLogo.setScaleX(0.7f);
        binding.ivLogo.setScaleY(0.7f);

        binding.ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        binding.tvAppName.setAlpha(0f);

        binding.tvAppName.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (binding != null) {
            binding = null;
        }
    }
}
