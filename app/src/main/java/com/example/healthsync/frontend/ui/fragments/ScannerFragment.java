package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.healthsync.databinding.FragmentScannerBinding;

public class ScannerFragment extends Fragment {
    private FragmentScannerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        startScanAnimation();
        return binding.getRoot();
    }

    private void startScanAnimation() {
        TranslateAnimation animation = new TranslateAnimation(
                0, 0,
                0, 750 // Approximately the height of the scanner frame
        );
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        binding.scanBeam.startAnimation(animation);
    }
}
