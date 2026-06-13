package com.example.healthsync.frontend.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.healthsync.R;
import com.example.healthsync.databinding.ActivityMainBinding;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.healthsync.backend.health.StepTrackingWorker;
import java.util.concurrent.TimeUnit;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.healthsync.backend.health.StepSensorManager;
import com.example.healthsync.backend.data.repository.StepRepository;
import com.example.healthsync.backend.data.local.MealDatabase;
import com.example.healthsync.frontend.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private StepSensorManager stepSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }

        checkPermissions();
        scheduleStepTracking();
        initStepSensor();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
            }
        }
    }

    private void initStepSensor() {
        StepRepository repo = new StepRepository(
                MealDatabase.Companion.getDatabase(this).stepDao(),
                new PreferenceManager(this)
        );
        stepSensorManager = new StepSensorManager(this, steps -> {
            repo.updateStepsSync(steps);
            return kotlin.Unit.INSTANCE;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensorManager != null) {
            stepSensorManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepSensorManager != null) {
            stepSensorManager.stop();
        }
    }

    private void scheduleStepTracking() {
        PeriodicWorkRequest stepWorkRequest = new PeriodicWorkRequest.Builder(
                StepTrackingWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "StepTrackingWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                stepWorkRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
