package com.example.healthsync.frontend.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthsync.R;
import com.example.healthsync.backend.data.local.SleepEntryEntity;
import com.example.healthsync.databinding.FragmentWellnessBinding;
import com.example.healthsync.backend.ui.viewmodels.HealthViewModel;
import com.example.healthsync.backend.ui.viewmodels.ViewModelFactory;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.backend.engines.SleepAnalyticsEngine;
import com.example.healthsync.backend.engines.WellnessAnalyticsEngine;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WellnessFragment extends Fragment {

    private FragmentWellnessBinding binding;
    private PreferenceManager prefs;
    private HealthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWellnessBinding.inflate(inflater, container, false);
        prefs = new PreferenceManager(requireContext());
        
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(HealthViewModel.class);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSleepChart();
        setupClickListeners();
        observeData();
    }

    private void observeData() {
        // Source of Truth: Latest Sleep Entry for Header
        viewModel.getLatestSleepEntry().observe(getViewLifecycleOwner(), entry -> {
            if (entry != null) {
                Log.d("LATEST_SLEEP_HEADER", "Hours: " + entry.getSleepHours() + 
                    ", Minutes: " + entry.getSleepMinutes() + 
                    ", Score: " + entry.getSleepScore() + 
                    ", TS: " + entry.getTimestamp());
                
                String durationText = entry.getSleepMinutes() > 0 ? 
                    entry.getSleepHours() + "h " + entry.getSleepMinutes() + "m Last Night" : 
                    entry.getSleepHours() + "h Last Night";
                
                binding.tvSleepValue.setText(durationText);
                binding.tvSleepScore.setText(String.format(Locale.getDefault(), "Score: %d", entry.getSleepScore()));
                
                // Trigger analytics refresh when sleep data changes
                refreshWellnessAnalytics();
            } else {
                binding.tvSleepValue.setText("0h Last Night");
                binding.tvSleepScore.setText("Score: --");
            }
        });

        // Source of Truth: Weekly Entries for Chart and Analytics
        viewModel.getWeeklySleepEntries().observe(getViewLifecycleOwner(), entries -> {
            Log.d("LATEST_SLEEP_CHART", "Weekly entries loaded: " + entries.size());
            updateChartAndAnalytics(entries);
        });
    }

    private void setupSleepChart() {
        BarChart chart = binding.sleepChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        xAxis.setGranularity(1f);

        chart.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(requireContext(), R.color.divider_dark));
        chart.getAxisRight().setEnabled(false);
    }

    private void updateChartAndAnalytics(List<SleepEntryEntity> entries) {
        SleepAnalyticsEngine.WeeklyStats stats = SleepAnalyticsEngine.INSTANCE.calculateWeeklyStats(entries);
        
        List<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < stats.getChartValues().length; i++) {
            barEntries.add(new BarEntry(i, stats.getChartValues()[i]));
        }

        Log.d("CHART_UPDATE", "Bar values generated for chart from " + entries.size() + " entries");

        XAxis xAxis = binding.sleepChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(stats.getDayLabels()));

        BarDataSet dataSet = new BarDataSet(barEntries, "Sleep Hours");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.accent_cyan));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_white));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        binding.sleepChart.setData(data);
        binding.sleepChart.notifyDataSetChanged();
        binding.sleepChart.invalidate();
        binding.sleepChart.animateY(1000);

        // Sync Analytics UI with Chart Data from Engine
        binding.tvAvgSleep.setText(String.format(Locale.getDefault(), "%.1fh", stats.getAverageHours()));
        binding.tvBestSleepDay.setText(stats.getBestDay());
        binding.tvSleepTrend.setText(stats.getTrend());
        
        if (stats.getTrend().equals("Improving")) {
            binding.tvSleepTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_cyan));
        } else if (stats.getTrend().equals("Declining")) {
            binding.tvSleepTrend.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
        } else {
            binding.tvSleepTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white));
        }

        Log.d("LATEST_SLEEP_ANALYTICS", "Avg: " + stats.getAverageHours() + ", Best: " + stats.getBestDay());
    }

    private void refreshWellnessAnalytics() {
        WellnessAnalyticsEngine.WellnessReport report = WellnessAnalyticsEngine.INSTANCE.generateReport(
            prefs.getSleepScore(), prefs.getStressScore()
        );
        
        binding.tvWellnessStatus.setText("Overall Status: " + report.getStatus());
        binding.tvWellnessInsight.setText(report.getInsight());
    }

    private void loadInitialNonSleepData() {
        updateHydrationUI();
        binding.tvStressStatus.setText("Feeling " + prefs.getStressLevel());
        binding.tvStressScore.setText("Score: " + prefs.getStressScore() + "/100");
        refreshWellnessAnalytics();
    }

    private void setupClickListeners() {
        binding.btnAdd250.setOnClickListener(v -> addWater(250));
        binding.btnAdd500.setOnClickListener(v -> addWater(500));
        binding.btnAdd1000.setOnClickListener(v -> addWater(1000));
        
        binding.btnLogMood.setOnClickListener(v -> showMoodDialog());
        binding.btnUpdateSleep.setOnClickListener(v -> showSleepDialog());
    }

    private void showSleepDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_sleep, null);
        NumberPicker hp = dialogView.findViewById(R.id.picker_hours);
        NumberPicker mp = dialogView.findViewById(R.id.picker_minutes);
        
        hp.setMinValue(0); hp.setMaxValue(15); hp.setValue(7);
        mp.setMinValue(0); mp.setMaxValue(59); mp.setValue(30);

        new AlertDialog.Builder(requireContext())
            .setTitle("Log Sleep Duration")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                int h = hp.getValue();
                int m = mp.getValue();
                float totalHours = h + (m / 60f);
                int score = (int) (Math.min(1.0f, totalHours / 8.0f) * 100);

                viewModel.logSleepEntry(h, m, score);
                Toast.makeText(requireContext(), "Sleep Logged!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showMoodDialog() {
        String[] moods = {"Happy 😀", "Calm 🙂", "Neutral 😐", "Stressed 😟", "Sad 😢", "Angry 😡"};
        new AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setItems(moods, (dialog, which) -> {
                String mood = moods[which].split(" ")[0];
                viewModel.logMood(mood, "");
                Toast.makeText(requireContext(), "Mood Logged: " + mood, Toast.LENGTH_SHORT).show();
                loadInitialNonSleepData();
            }).show();
    }

    private void addWater(int amount) {
        viewModel.logWater(amount);
        updateHydrationUI();
    }

    private void updateHydrationUI() {
        if (binding == null) return;
        int current = prefs.getWaterIntake();
        int goal = prefs.getWaterGoal();
        if (goal <= 0) goal = 3000;
        
        binding.tvHydrationValue.setText(String.format(Locale.getDefault(), "%.1fL / %.1fL", current/1000f, goal/1000f));
        binding.pbHydration.setProgress(Math.min(100, (int)((current * 100f) / goal)));
        binding.tvRemainingWater.setText(String.format(Locale.getDefault(), "Remaining: %.1fL", Math.max(0, (goal - current)/1000f)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInitialNonSleepData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
