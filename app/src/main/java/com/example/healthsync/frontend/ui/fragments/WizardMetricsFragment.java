package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentWizardMetricsBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;

public class WizardMetricsFragment extends Fragment {

    private FragmentWizardMetricsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardMetricsBinding.inflate(inflater, container, false);

        // Initial values
        UserDataHolder.userProfile.height =
                binding.sliderHeight.getValue();

        UserDataHolder.userProfile.weight =
                binding.sliderWeight.getValue();

        // Height
        binding.sliderHeight.addOnChangeListener(
                (slider, value, fromUser) ->
                        UserDataHolder.userProfile.height = value
        );

        // Weight
        binding.sliderWeight.addOnChangeListener(
                (slider, value, fromUser) ->
                        UserDataHolder.userProfile.weight = value
        );

        return binding.getRoot();
    }
}
