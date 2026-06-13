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
import com.example.healthsync.databinding.FragmentHealthScoreBinding;

import java.util.ArrayList;
import java.util.List;

import app.futured.donut.DonutSection;

public class HealthScoreFragment extends Fragment {

    private FragmentHealthScoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthScoreBinding.inflate(inflater, container, false);
        setupScoreDonut();
        return binding.getRoot();
    }

    private void setupScoreDonut() {
        List<DonutSection> sections = new ArrayList<>();
        sections.add(new DonutSection(
                "Score",
                ContextCompat.getColor(requireContext(), R.color.cyan_glow),
                88f
        ));
        binding.scoreDonut.setCap(100f);
        binding.scoreDonut.submitData(sections);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
