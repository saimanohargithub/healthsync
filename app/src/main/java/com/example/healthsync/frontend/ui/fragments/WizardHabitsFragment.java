package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentWizardHabitsBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;

public class WizardHabitsFragment extends Fragment {

    private FragmentWizardHabitsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardHabitsBinding.inflate(inflater, container, false);

        // Default values
        UserDataHolder.userProfile.waterGoal = binding.sliderWater.getValue();
        UserDataHolder.userProfile.sleepGoal = binding.sliderSleep.getValue();

        binding.sliderWater.addOnChangeListener(
                (slider, value, fromUser) ->
                        UserDataHolder.userProfile.waterGoal = value
        );

        binding.sliderSleep.addOnChangeListener(
                (slider, value, fromUser) ->
                        UserDataHolder.userProfile.sleepGoal = value
        );

        return binding.getRoot();
    }
}
