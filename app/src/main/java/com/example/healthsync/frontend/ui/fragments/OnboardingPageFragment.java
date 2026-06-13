package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_LAYOUT_RES = "layout_res";

    public static OnboardingPageFragment newInstance(@LayoutRes int layoutRes) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES, layoutRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutRes = getArguments() != null ? getArguments().getInt(ARG_LAYOUT_RES) : 0;
        return inflater.inflate(layoutRes, container, false);
    }
}
