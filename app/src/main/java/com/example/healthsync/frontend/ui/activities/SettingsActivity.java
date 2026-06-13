package com.example.healthsync.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.healthsync.R;
import com.example.healthsync.backend.data.local.MealDatabase;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.example.healthsync.frontend.utils.UserDataHolder;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        findViewById(R.id.card_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
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

            PreferenceManager prefs = new PreferenceManager(this);
            prefs.clear();
            Log.d("LOGOUT", "PREFERENCES_CLEARED");

            UserDataHolder.userProfile = new UserProfile();
            Log.d("LOGOUT", "USER_DATA_HOLDER_RESET");

            new Thread(() -> {
                try {
                    MealDatabase.Companion.getDatabase(this).clearAllTables();
                    Log.d("LOGOUT", "ROOM_DB_CLEARED");
                } catch (Exception e) {
                    Log.e("LOGOUT", "Error clearing Room DB", e);
                }
                
                runOnUiThread(() -> {
                    Log.d("LOGOUT", "NAVIGATE_LOGIN");
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }).start();
        } catch (Exception e) {
            Log.e("LOGOUT", "Logout failed", e);
            Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
