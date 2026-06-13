package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.healthsync.R;
import com.example.healthsync.databinding.FragmentWizardGenderBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;

public class WizardGenderFragment extends Fragment {

    private FragmentWizardGenderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardGenderBinding.inflate(
                inflater,
                container,
                false
        );

        // Restore previous selection
        if ("Male".equals(UserDataHolder.userProfile.gender)) {
            selectMale();
        } else if ("Female".equals(UserDataHolder.userProfile.gender)) {
            selectFemale();
        }

        binding.cardMale.setOnClickListener(v -> {
            UserDataHolder.userProfile.gender = "Male";
            selectMale();
        });

        binding.cardFemale.setOnClickListener(v -> {
            UserDataHolder.userProfile.gender = "Female";
            selectFemale();
        });

        return binding.getRoot();
    }

    private void selectMale() {

        binding.cardMale.setStrokeWidth(4);
        binding.cardMale.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.cyan_glow
                )
        );

        binding.cardFemale.setStrokeWidth(2);
        binding.cardFemale.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );
    }

    private void selectFemale() {

        binding.cardFemale.setStrokeWidth(4);
        binding.cardFemale.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.cyan_glow
                )
        );

        binding.cardMale.setStrokeWidth(2);
        binding.cardMale.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
