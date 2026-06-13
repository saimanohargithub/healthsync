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
import com.example.healthsync.databinding.FragmentWizardActivityBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;

public class WizardActivityFragment extends Fragment {

    private FragmentWizardActivityBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardActivityBinding.inflate(
                inflater,
                container,
                false
        );

        restoreSelection();

        binding.cardSedentary.setOnClickListener(v -> {

            UserDataHolder.userProfile.activityLevel =
                    "Sedentary";

            selectSedentary();
        });

        binding.cardModeratelyActive.setOnClickListener(v -> {

            UserDataHolder.userProfile.activityLevel =
                    "Moderately Active";

            selectModeratelyActive();
        });

        binding.cardHighlyActive.setOnClickListener(v -> {

            UserDataHolder.userProfile.activityLevel =
                    "Highly Active";

            selectHighlyActive();
        });

        return binding.getRoot();
    }

    private void selectSedentary() {

        binding.cardSedentary.setStrokeWidth(4);
        binding.cardSedentary.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.cyan_glow
                )
        );

        binding.cardModeratelyActive.setStrokeWidth(2);
        binding.cardModeratelyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );

        binding.cardHighlyActive.setStrokeWidth(2);
        binding.cardHighlyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );
    }

    private void selectModeratelyActive() {

        binding.cardModeratelyActive.setStrokeWidth(4);
        binding.cardModeratelyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.cyan_glow
                )
        );

        binding.cardSedentary.setStrokeWidth(2);
        binding.cardSedentary.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );

        binding.cardHighlyActive.setStrokeWidth(2);
        binding.cardHighlyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );
    }

    private void selectHighlyActive() {

        binding.cardHighlyActive.setStrokeWidth(4);
        binding.cardHighlyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.cyan_glow
                )
        );

        binding.cardSedentary.setStrokeWidth(2);
        binding.cardSedentary.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );

        binding.cardModeratelyActive.setStrokeWidth(2);
        binding.cardModeratelyActive.setStrokeColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.glass_border
                )
        );
    }

    private void restoreSelection() {

        String activity =
                UserDataHolder.userProfile.activityLevel;

        if (activity == null) {
            return;
        }

        switch (activity) {

            case "Sedentary":
                selectSedentary();
                break;

            case "Moderately Active":
                selectModeratelyActive();
                break;

            case "Highly Active":
                selectHighlyActive();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
