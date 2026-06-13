package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentWizardMedicalBinding;
import com.example.healthsync.frontend.utils.UserDataHolder;

import java.util.ArrayList;
import java.util.List;

public class WizardMedicalFragment extends Fragment {

    private FragmentWizardMedicalBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardMedicalBinding.inflate(
                inflater,
                container,
                false
        );

        restoreMedicalData();

        binding.cbDiabetes.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveMedical());

        binding.cbHypertension.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveMedical());

        binding.cbHeart.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveMedical());

        binding.cbObesity.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveMedical());

        binding.etOtherConditions.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {

                        saveMedical();
                    }
                });

        return binding.getRoot();
    }

    private void saveMedical() {

        ArrayList<String> conditions =
                new ArrayList<>();

        if (binding.cbDiabetes.isChecked()) {
            conditions.add("Diabetes");
        }

        if (binding.cbHypertension.isChecked()) {
            conditions.add("Hypertension");
        }

        if (binding.cbHeart.isChecked()) {
            conditions.add("Heart Condition");
        }

        if (binding.cbObesity.isChecked()) {
            conditions.add("Obesity");
        }

        String other =
                binding.etOtherConditions
                        .getText()
                        .toString()
                        .trim();

        if (!other.isEmpty()) {
            conditions.add(other);
        }

        UserDataHolder.userProfile.medicalConditions =
                conditions;
    }

    private void restoreMedicalData() {

        List<String> conditions =
                UserDataHolder.userProfile.medicalConditions;

        if (conditions == null) {
            return;
        }

        if (conditions.contains("Diabetes")) {
            binding.cbDiabetes.setChecked(true);
        }

        if (conditions.contains("Hypertension")) {
            binding.cbHypertension.setChecked(true);
        }

        if (conditions.contains("Heart Condition")) {
            binding.cbHeart.setChecked(true);
        }

        if (conditions.contains("Obesity")) {
            binding.cbObesity.setChecked(true);
        }

        for (String condition : conditions) {

            if (!condition.equals("Diabetes")
                    && !condition.equals("Hypertension")
                    && !condition.equals("Heart Condition")
                    && !condition.equals("Obesity")) {

                binding.etOtherConditions.setText(
                        condition
                );
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveMedical();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
