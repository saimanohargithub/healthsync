package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WizardFragment extends Fragment {

    private static final String ARG_LAYOUT_RES = "layout_res";
    private int layoutRes;

    public static WizardFragment newInstance(@LayoutRes int layoutRes) {
        WizardFragment fragment = new WizardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES, layoutRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            layoutRes = getArguments().getInt(ARG_LAYOUT_RES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutRes, container, false);
    }
}
