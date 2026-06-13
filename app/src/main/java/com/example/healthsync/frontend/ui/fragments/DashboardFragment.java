package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.healthsync.R;
import com.example.healthsync.databinding.FragmentDashboardBinding;
import com.example.healthsync.backend.ui.viewmodels.HealthViewModel;
import com.example.healthsync.backend.ui.viewmodels.ViewModelFactory;
import com.example.healthsync.backend.engines.HealthScoreEngine;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.backend.ui.viewmodels.StepViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.futured.donut.DonutSection;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private PreferenceManager prefs;
    private HealthViewModel viewModel;
    private StepViewModel stepViewModel;
    private float currentSleepHours = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        prefs = new PreferenceManager(requireContext());
        
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(HealthViewModel.class);
        stepViewModel = new ViewModelProvider(this, factory).get(StepViewModel.class);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        // Observe Latest Sleep for Real-time Dashboard Updates
        viewModel.getLatestSleepEntry().observe(getViewLifecycleOwner(), entry -> {
            if (entry != null) {
                android.util.Log.d("DASHBOARD_SLEEP", "LATEST_SLEEP_LOADED: " + entry.getSleepHours() + "h");
                updateSleepUI(entry.getSleepHours(), entry.getSleepMinutes(), entry.getSleepScore());
            } else {
                android.util.Log.d("DASHBOARD_SLEEP", "No sleep entry found");
                updateSleepUI(0, 0, 0);
            }
        });

        stepViewModel.getAllSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps != null && !steps.isEmpty()) {
                int todaySteps = steps.get(0).getSteps();
                updateStepsUI(todaySteps);
            }
        });

        // Since we are in Java and using StateFlow for others, we can update onResume or use a bridge.
        updateUI();
    }

    private void updateSleepUI(int hours, int minutes, int score) {
        if (binding == null) return;
        
        currentSleepHours = hours + (minutes / 60f);
        int sleepPercent = (int) (Math.min(1.0f, currentSleepHours / 8.0f) * 100);
        
        android.util.Log.d("DASHBOARD_SLEEP", "SLEEP_PROGRESS: " + sleepPercent + "%");

        String sleepText = minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        binding.tvSleepHours.setText(sleepText + " / 8h");
        binding.pbSleepMini.setProgress(sleepPercent);
        binding.tvSleepPercent.setText(sleepPercent + "% completed");
        
        // Update Daily Goal Progress Row
        binding.pbGoalSleep.setProgress(sleepPercent);
        binding.tvGoalSleepPercent.setText(sleepPercent + "%");
        
        // Update Health Score since sleep changed
        updateHealthScore();
    }

    private void updateStepsUI(int steps) {
        if (binding == null) return;
        int stepGoal = 10000;
        int stepPercent = Math.min(100, (int) ((steps * 100.0f) / stepGoal));
        binding.pbGoalSteps.setProgress(stepPercent);
        binding.tvGoalStepsPercent.setText(stepPercent + "%");
    }

    private void updateHealthScore() {
        if (binding == null) return;
        
        int calories = prefs.getTodayCalories();
        int waterMl = prefs.getWaterIntake();
        int waterGoal = prefs.getWaterGoal();
        if (waterGoal <= 0) waterGoal = 3000;

        int healthScore = HealthScoreEngine.INSTANCE.calculateScore(
            waterMl, waterGoal, currentSleepHours, prefs.getStressScore(), calories, prefs.getTodayProtein()
        );
        
        android.util.Log.d("DASHBOARD_SLEEP", "Dashboard updated with score: " + healthScore);
        binding.tvScoreBadge.setText(String.format(Locale.getDefault(), "%d / 100", healthScore));

        // Update Donut Chart
        binding.donutView.setCap(100f);
        List<DonutSection> sections = new ArrayList<>();
        sections.add(new DonutSection(
            "Score",
            ContextCompat.getColor(requireContext(), R.color.accent_cyan),
            (float) healthScore
        ));
        binding.donutView.submitData(sections);
    }

    private void updateUI() {
        if (binding == null) return;
        
        // Header
        String name = prefs.getUserName();
        binding.tvGreeting.setText("Good morning, " + (name != null && !name.isEmpty() ? name : "User"));

        // 1. Calories
        int calories = prefs.getTodayCalories();
        int calGoal = 2200; 
        int calPercent = Math.min(100, (int) ((calories * 100.0f) / calGoal));
        binding.tvCaloriesValue.setText(String.format(Locale.getDefault(), "%d / %d", calories, calGoal));
        binding.pbCaloriesMini.setProgress(calPercent);
        binding.tvCaloriesPercent.setText(String.format(Locale.getDefault(), "%d%% completed", calPercent));
        binding.pbGoalCalories.setProgress(calPercent);
        binding.tvGoalCaloriesPercent.setText(String.format(Locale.getDefault(), "%d%%", calPercent));

        // 2. Water
        int waterMl = prefs.getWaterIntake();
        int waterGoal = prefs.getWaterGoal();
        if (waterGoal <= 0) waterGoal = 3000;
        int waterPercent = Math.min(100, (int) ((waterMl * 100.0f) / waterGoal));
        binding.tvWaterValue.setText(String.format(Locale.getDefault(), "%.1fL / %.1fL", waterMl/1000f, waterGoal/1000f));
        binding.pbWaterMini.setProgress(waterPercent);
        binding.tvWaterPercent.setText(String.format(Locale.getDefault(), "%d%% completed", waterPercent));
        binding.pbGoalWater.setProgress(waterPercent);
        binding.tvGoalWaterPercent.setText(String.format(Locale.getDefault(), "%d%%", waterPercent));

        // 3. Sleep is now updated via observeViewModel() to ensure latest data sync

        // 4. Nutrients
        binding.tvProteinValue.setText(String.format(Locale.getDefault(), "%.0fg", prefs.getTodayProtein()));
        binding.tvCarbsValue.setText(String.format(Locale.getDefault(), "%.0fg", prefs.getTodayCarbs()));
        binding.tvFatValue.setText(String.format(Locale.getDefault(), "%.0fg", prefs.getTodayFat()));

        // 5. Health Score & Chart
        updateHealthScore();

        // 6. Stress
        int stressScore = prefs.getStressScore();
        binding.tvStressLevel.setText(prefs.getStressLevel());
        binding.tvStressScore.setText("Score " + stressScore);
        binding.pbStressMini.setProgress(stressScore);
    }

    private void setupClickListeners() {
        binding.cardScanMeal.setOnClickListener(v -> {
             android.content.Intent intent = new android.content.Intent(requireContext(), com.example.healthsync.frontend.ui.activities.MealScannerActivity.class);
             startActivity(intent);
        });
        binding.cardLogWater.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.wellnessFragment));
        binding.cardPredictive.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_home_to_predictive));
        binding.cardAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
