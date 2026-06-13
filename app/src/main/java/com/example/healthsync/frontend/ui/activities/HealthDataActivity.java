package com.example.healthsync.frontend.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.healthsync.R;
import com.example.healthsync.backend.ui.viewmodels.HealthDataViewModel;
import com.example.healthsync.backend.ui.viewmodels.ViewModelFactory;
import java.util.Locale;

public class HealthDataActivity extends AppCompatActivity {

    private HealthDataViewModel viewModel;
    private TextView tvFullName, tvEmail, tvMetrics, tvSummaries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_data);

        tvFullName = findViewById(R.id.tv_full_name);
        tvEmail = findViewById(R.id.tv_email);
        tvMetrics = findViewById(R.id.tv_metrics);
        tvSummaries = findViewById(R.id.tv_summaries);

        ViewModelFactory factory = new ViewModelFactory(this);
        viewModel = new ViewModelProvider(this, factory).get(HealthDataViewModel.class);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                tvFullName.setText("Name: " + user.getName());
                tvMetrics.setText(String.format(Locale.getDefault(),
                        "Age: %d\nHeight: %.0fcm\nWeight: %.0fkg",
                        user.getAge(), user.getHeightCm(), user.getWeightKg()));
            }
        });

        viewModel.getUserEmail().observe(this, email -> {
            tvEmail.setText("Email: " + email);
        });

        viewModel.getBmi().observe(this, bmi -> {
            String current = tvMetrics.getText().toString();
            tvMetrics.setText(current + String.format(Locale.getDefault(), "\nBMI: %.1f", bmi));
        });

        viewModel.getWaterIntake().observe(this, water -> updateSummaries());
        viewModel.getSleepLogs().observe(this, logs -> updateSummaries());
        viewModel.getMoodHistory().observe(this, moods -> updateSummaries());
        viewModel.getMealHistory().observe(this, meals -> updateSummaries());
    }

    private void updateSummaries() {
        Integer water = viewModel.getWaterIntake().getValue();
        float waterL = (water != null ? water : 0) / 1000f;
        
        java.util.List<com.example.healthsync.backend.data.local.SleepEntryEntity> sleepLogs = viewModel.getSleepLogs().getValue();
        float avgSleep = 0;
        if (sleepLogs != null && !sleepLogs.isEmpty()) {
            float total = 0;
            for (com.example.healthsync.backend.data.local.SleepEntryEntity log : sleepLogs) {
                total += log.getSleepHours() + (log.getSleepMinutes() / 60f);
            }
            avgSleep = total / sleepLogs.size();
        }

        java.util.List<com.example.healthsync.backend.data.local.MoodLog> moodHistory = viewModel.getMoodHistory().getValue();
        String latestMood = (moodHistory != null && !moodHistory.isEmpty()) ? moodHistory.get(0).getMood() : "N/A";

        java.util.List<com.example.healthsync.backend.data.local.MealEntity> mealHistory = viewModel.getMealHistory().getValue();
        int mealCount = (mealHistory != null) ? mealHistory.size() : 0;

        tvSummaries.setText(String.format(Locale.getDefault(),
                "Water Intake: %.1fL\nSleep Average: %.1fh\nMood Summary: %s\nMeals Tracked: %d",
                waterL, avgSleep, latestMood, mealCount));
    }
}
