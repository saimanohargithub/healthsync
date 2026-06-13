package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentWaterBinding;

public class WaterFragment extends Fragment {

    private FragmentWaterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWaterBinding.inflate(inflater, container, false);
        
        binding.btn250ml.setOnClickListener(v -> {
            binding.waterGlass.setFillPercent(0.9f);
            binding.tvWaterAmount.setText("2.35L / 3.0L");
            binding.tvWaterPercent.setText("78% of daily goal");
        });
        
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
