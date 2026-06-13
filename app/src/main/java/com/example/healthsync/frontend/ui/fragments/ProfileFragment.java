package com.example.healthsync.frontend.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthsync.backend.data.local.MealDatabase;
import com.example.healthsync.backend.ui.viewmodels.ProfileViewModel;
import com.example.healthsync.backend.ui.viewmodels.ViewModelFactory;
import com.example.healthsync.databinding.FragmentProfileBinding;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.ui.activities.HealthDataActivity;
import com.example.healthsync.frontend.ui.activities.LoginActivity;
import com.example.healthsync.frontend.ui.activities.SettingsActivity;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.frontend.utils.UserDataHolder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(super.getContext() != null ? view : null, savedInstanceState);
        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUserDisplayName.setText(user.getName());
                binding.tvAgeVal.setText(String.valueOf(user.getAge()));
                binding.tvHeightVal.setText(String.format(Locale.getDefault(), "%.0fcm", user.getHeightCm()));
                binding.tvWeightVal.setText(String.format(Locale.getDefault(), "%.0fkg", user.getWeightKg()));
            }
        });

        viewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            binding.tvUserEmail.setText(email);
        });

        viewModel.getBmi().observe(getViewLifecycleOwner(), bmi -> {
            binding.tvBmiVal.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        });

        viewModel.getTotalWaterLogged().observe(getViewLifecycleOwner(), water -> {
            binding.tvStatWater.setText(String.format(Locale.getDefault(), "%.1fL", water));
        });

        viewModel.getCompletedChallenges().observe(getViewLifecycleOwner(), count -> {
            binding.tvStatChallenges.setText(String.valueOf(count));
        });

        viewModel.getDaysActive().observe(getViewLifecycleOwner(), days -> {
            binding.tvStatDays.setText(String.valueOf(days));
        });
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        binding.cardHealthData.setOnClickListener(v -> {
            Log.d("PROFILE", "HEALTH_DATA_OPEN");
            startActivity(new Intent(requireContext(), HealthDataActivity.class));
        });
        
        binding.cardSettings.setOnClickListener(v -> {
            Log.d("PROFILE", "SETTINGS_OPEN");
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        try {
            FirebaseAuth.getInstance().signOut();
            Log.d("LOGOUT", "FIREBASE_SIGNOUT_SUCCESS");

            PreferenceManager prefs = new PreferenceManager(requireContext());
            prefs.clear();
            Log.d("LOGOUT", "PREFERENCES_CLEARED");

            UserDataHolder.userProfile = new UserProfile();
            Log.d("LOGOUT", "USER_DATA_HOLDER_RESET");

            new Thread(() -> {
                try {
                    MealDatabase.Companion.getDatabase(requireContext()).clearAllTables();
                    Log.d("LOGOUT", "ROOM_DB_CLEARED");
                } catch (Exception e) {
                    Log.e("LOGOUT", "Error clearing Room DB", e);
                }
                
                requireActivity().runOnUiThread(() -> {
                    Log.d("LOGOUT", "NAVIGATE_LOGIN");
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                });
            }).start();
        } catch (Exception e) {
            Log.e("LOGOUT", "Logout failed", e);
            Toast.makeText(requireContext(), "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
