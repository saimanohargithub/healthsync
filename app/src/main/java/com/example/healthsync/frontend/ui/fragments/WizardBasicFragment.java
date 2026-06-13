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

import com.example.healthsync.databinding.FragmentWizardBasicBinding;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.frontend.utils.UserDataHolder;

public class WizardBasicFragment extends Fragment {

    private FragmentWizardBasicBinding binding;
    private PreferenceManager prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWizardBasicBinding.inflate(inflater, container, false);

        prefs = new PreferenceManager(requireContext());

        binding.etName.setText(prefs.getUserName());

        binding.etName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                prefs.setUserName(s.toString());

                UserDataHolder.userProfile.name =
                        s.toString();
            }
        });

        binding.etAge.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                try {

                    UserDataHolder.userProfile.age =
                            Integer.parseInt(s.toString());

                } catch (Exception e) {

                    UserDataHolder.userProfile.age = 0;
                }
            }
        });

        return binding.getRoot();
    }
}
