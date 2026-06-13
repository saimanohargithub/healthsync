package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentWizardGoalsBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;
import com.google.android.material.chip.Chip;

public class WizardGoalsFragment extends Fragment {

    private FragmentWizardGoalsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardGoalsBinding.inflate(
                inflater,
                container,
                false
        );

        restoreSelection();

        int childCount = binding.cgGoals.getChildCount();

        for (int i = 0; i < childCount; i++) {

            View view = binding.cgGoals.getChildAt(i);

            if (view instanceof Chip) {

                Chip chip = (Chip) view;

                chip.setOnClickListener(v -> {

                    UserDataHolder.userProfile.goal =
                            chip.getText().toString();

                    clearAllSelections();

                    chip.setChecked(true);
                });
            }
        }

        return binding.getRoot();
    }

    private void clearAllSelections() {

        for (int i = 0; i < binding.cgGoals.getChildCount(); i++) {

            View child = binding.cgGoals.getChildAt(i);

            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void restoreSelection() {

        String savedGoal =
                UserDataHolder.userProfile.goal;

        if (savedGoal == null) {
            return;
        }

        for (int i = 0; i < binding.cgGoals.getChildCount(); i++) {

            View child = binding.cgGoals.getChildAt(i);

            if (child instanceof Chip) {

                Chip chip = (Chip) child;

                if (savedGoal.equals(
                        chip.getText().toString())) {

                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
