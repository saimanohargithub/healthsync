package com.example.healthsync.frontend.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthsync.databinding.FragmentSignupBinding;
import com.example.healthsync.frontend.ui.activities.LoginActivity;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnSignup.setOnClickListener(v -> handleSignup());
        
        binding.tvLoginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        binding.btnGoogle.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Google Sign Up coming soon", Toast.LENGTH_SHORT).show()
        );

        binding.btnApple.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Apple Sign Up coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleSignup() {
        if (binding.tilName.getEditText() == null || binding.tilEmail.getEditText() == null ||
            binding.tilPassword.getEditText() == null || binding.tilConfirmPassword.getEditText() == null) {
            return;
        }

        String name = binding.tilName.getEditText().getText().toString();
        String email = binding.tilEmail.getEditText().getText().toString();
        String password = binding.tilPassword.getEditText().getText().toString();
        String confirmPassword = binding.tilConfirmPassword.getEditText().getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(getContext(), "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Account Created Successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
