package com.example.healthsync.frontend.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.healthsync.R;
import com.example.healthsync.databinding.FragmentNutritionBinding;
import com.example.healthsync.backend.data.model.MealPlanModel;
import com.example.healthsync.frontend.ui.activities.MealScannerActivity;
import com.example.healthsync.frontend.ui.adapters.MealLogAdapter;
import com.example.healthsync.backend.ui.viewmodels.MealPlanViewModel;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class NutritionFragment extends Fragment {

    private FragmentNutritionBinding binding;
    private PreferenceManager prefs;
    private MealPlanViewModel viewModel;
    private MealLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNutritionBinding.inflate(inflater, container, false);
        prefs = new PreferenceManager(requireContext());
        viewModel = new ViewModelProvider(this).get(MealPlanViewModel.class);
        
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        
        viewModel.fetchMealPlan(false);
        viewModel.fetchTodayMeals();
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new MealLogAdapter();
        binding.rvMealLog.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMealLog.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMealPlan().observe(getViewLifecycleOwner(), this::updateMealPlanUI);
        viewModel.getTodayMeals().observe(getViewLifecycleOwner(), meals -> {
            adapter.setMeals(meals);
            binding.tvEmptyLog.setVisibility(meals.isEmpty() ? View.VISIBLE : View.GONE);
            updateNutritionUI();
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> 
            binding.loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }

    private void updateMealPlanUI(MealPlanModel plan) {
        if (plan == null) return;

        binding.tvTargetCalories.setText(String.format(Locale.getDefault(), "%d kcal target", plan.totalCalories));
        binding.tvTargetProtein.setText(String.format(Locale.getDefault(), "%dg protein target", plan.totalProtein));

        updateMealCard(binding.cardBreakfast, binding.tvBreakfastTime, binding.tvBreakfastItems, binding.tvBreakfastMacros, binding.btnCompleteBreakfast, plan.breakfast);
        updateMealCard(binding.cardLunch, binding.tvLunchTime, binding.tvLunchItems, binding.tvLunchMacros, binding.btnCompleteLunch, plan.lunch);
        updateMealCard(binding.cardDinner, binding.tvDinnerTime, binding.tvDinnerItems, binding.tvDinnerMacros, binding.btnCompleteDinner, plan.dinner);
        updateMealCard(binding.cardSnack, binding.tvSnackTime, binding.tvSnackItems, binding.tvSnackMacros, binding.btnCompleteSnack, plan.snack);

        binding.tvHealthAdvice.setText(plan.healthAdvice);
    }

    private void updateMealCard(MaterialCardView card, android.widget.TextView time, android.widget.TextView items, android.widget.TextView macros, MaterialButton button, MealPlanModel.Meal meal) {
        if (meal == null) return;
        time.setText(meal.scheduledTime);
        items.setText(formatFoodItems(meal.foodItems));
        macros.setText(String.format(Locale.getDefault(), "%d kcal    |    %dg Protein", meal.calories, meal.protein));
        
        if (meal.isCompleted) {
            button.setEnabled(false);
            button.setText("✓ Completed");
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            card.setStrokeColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            card.setStrokeWidth(4);
        } else {
            button.setEnabled(true);
            button.setText("Mark Completed");
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_cyan));
            card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.divider_dark));
            card.setStrokeWidth(2);
        }
    }

    private String formatFoodItems(List<String> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) return "Waiting for AI suggestions...";
        StringBuilder sb = new StringBuilder();
        int maxVisible = 3;
        int count = Math.min(foodItems.size(), maxVisible);
        for (int i = 0; i < count; i++) {
            sb.append("• ").append(foodItems.get(i));
            if (i < count - 1) sb.append("\n");
        }
        if (foodItems.size() > maxVisible) {
            sb.append("\n+").append(foodItems.size() - maxVisible).append(" more");
        }
        return sb.toString();
    }

    private void updateNutritionUI() {
        int calories = prefs.getTodayCalories();
        int calGoal = 2200;
        binding.tvCaloriesConsumed.setText(String.format(Locale.getDefault(), "%d / %d kcal", calories, calGoal));
        
        float protein = prefs.getTodayProtein();
        float carbs = prefs.getTodayCarbs();
        float fat = prefs.getTodayFat();
        
        binding.pbProtein.setProgress(Math.min(100, (int)((protein * 100f) / 150f)));
        binding.pbCarbs.setProgress(Math.min(100, (int)((carbs * 100f) / 250f)));
        binding.pbFat.setProgress(Math.min(100, (int)((fat * 100f) / 70f)));
    }

    private void setupClickListeners() {
        binding.btnRegenerate.setOnClickListener(v -> viewModel.fetchMealPlan(true));

        binding.btnScanMealLog.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MealScannerActivity.class);
            startActivity(intent);
        });

        binding.btnCompleteBreakfast.setOnClickListener(v -> handleComplete(binding.btnCompleteBreakfast, "breakfast"));
        binding.btnCompleteLunch.setOnClickListener(v -> handleComplete(binding.btnCompleteLunch, "lunch"));
        binding.btnCompleteDinner.setOnClickListener(v -> handleComplete(binding.btnCompleteDinner, "dinner"));
        binding.btnCompleteSnack.setOnClickListener(v -> handleComplete(binding.btnCompleteSnack, "snack"));
    }

    private void handleComplete(MaterialButton button, String mealType) {
        if (viewModel.getMealPlan().getValue() == null) return;
        
        MealPlanModel.Meal meal = null;
        switch (mealType) {
            case "breakfast": meal = viewModel.getMealPlan().getValue().breakfast; break;
            case "lunch": meal = viewModel.getMealPlan().getValue().lunch; break;
            case "dinner": meal = viewModel.getMealPlan().getValue().dinner; break;
            case "snack": meal = viewModel.getMealPlan().getValue().snack; break;
        }
        
        if (meal != null && !meal.isCompleted) {
            button.setEnabled(false);
            button.setText("Completing...");
            viewModel.completeMeal(meal);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.fetchTodayMeals();
        updateNutritionUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
