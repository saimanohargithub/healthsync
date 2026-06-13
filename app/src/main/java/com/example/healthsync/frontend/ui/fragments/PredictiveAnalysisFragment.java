package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.healthsync.R;
import com.example.healthsync.databinding.FragmentPredictiveAnalysisBinding;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.backend.engines.RiskCalculator;

import java.util.Locale;

public class PredictiveAnalysisFragment extends Fragment {

    private FragmentPredictiveAnalysisBinding binding;
    private PreferenceManager prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPredictiveAnalysisBinding.inflate(inflater, container, false);
        prefs = new PreferenceManager(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNavigation();
        calculateAndDisplayPredictiveData();
    }

    private void setupNavigation() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void calculateAndDisplayPredictiveData() {
        float heightM = prefs.getUserHeight() / 100f;
        float bmi = (heightM > 0) ? prefs.getUserWeight() / (heightM * heightM) : 0f;
        
        float sleepHours = 0;
        try { sleepHours = Float.parseFloat(prefs.getSleepHours().replace("h", "")); } catch (Exception ignored) {}

        RiskCalculator.RiskResult risks = RiskCalculator.INSTANCE.calculateRisks(
            prefs.getUserAge(),
            bmi,
            prefs.getStressScore(),
            prefs.getTodaySodium(),
            sleepHours,
            prefs.getWaterIntake(),
            prefs.getWaterGoal()
        );

        binding.chipBMI.setText(String.format(Locale.getDefault(), "BMI %.1f", bmi));
        
        // Diabetes
        binding.progressDiabetes.setProgress(risks.getDiabetesRisk());
        binding.tvDiabetesRisk.setText(String.format(Locale.getDefault(), "%d%%", risks.getDiabetesRisk()));
        binding.tvDiabetesStatus.setText(getRiskLabel(risks.getDiabetesRisk()));
        binding.progressDiabetes.setIndicatorColor(getRiskColor(risks.getDiabetesRisk()));

        // Hypertension
        binding.progressHyp.setProgress(risks.getHypertensionRisk());
        binding.tvHypRisk.setText(String.format(Locale.getDefault(), "%d%%", risks.getHypertensionRisk()));
        binding.tvHypStatus.setText(getRiskLabel(risks.getHypertensionRisk()));
        binding.progressHyp.setIndicatorColor(getRiskColor(risks.getHypertensionRisk()));

        // Obesity
        binding.progressObesity.setProgress(risks.getObesityRisk());
        binding.tvObesityRisk.setText(String.format(Locale.getDefault(), "%d%%", risks.getObesityRisk()));
        binding.tvObesityStatus.setText(getRiskLabel(risks.getObesityRisk()));
        binding.progressObesity.setIndicatorColor(getRiskColor(risks.getObesityRisk()));

        // Overall
        binding.tvHealthStatusBadge.setText(risks.getOverallStatus());
        binding.tvAssessmentDescription.setText("Your overall status is " + risks.getOverallStatus() + " based on your current health trends.");
        binding.tvRecommendation.setText(risks.getRecommendation());
    }

    private String getRiskLabel(int risk) {
        if (risk < 30) return "Low Risk";
        if (risk < 60) return "Moderate Risk";
        return "High Risk";
    }

    private int getRiskColor(int risk) {
        if (risk < 30) return ContextCompat.getColor(requireContext(), R.color.risk_low);
        if (risk < 60) return ContextCompat.getColor(requireContext(), R.color.risk_moderate);
        return ContextCompat.getColor(requireContext(), R.color.risk_high);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
