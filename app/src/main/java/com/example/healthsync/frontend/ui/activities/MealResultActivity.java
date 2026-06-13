package com.example.healthsync.frontend.ui.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthsync.databinding.ActivityMealResultBinding;
import com.example.healthsync.frontend.firebase.FirestoreManager;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class MealResultActivity extends AppCompatActivity {

    private ActivityMealResultBinding binding;
    private PreferenceManager prefs;

    private String mealName = "Unknown Meal";
    private int calories = 0;
    private int healthScore = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMealResultBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        prefs = new PreferenceManager(this);

        String imagePath =
                getIntent().getStringExtra("image_path");

        String aiResult =
                getIntent().getStringExtra("ai_result");

        if (imagePath != null) {

            binding.ivMeal.setImageBitmap(
                    BitmapFactory.decodeFile(imagePath)
            );
        }

        if (aiResult != null && !aiResult.isEmpty()) {

            parseResult(aiResult);

        } else {

            Toast.makeText(
                    this,
                    "No nutrition data received",
                    Toast.LENGTH_SHORT
            ).show();
        }

        setupListeners();
    }

    private void parseResult(String result) {

        Map<String, String> data =
                new HashMap<>();

        String[] lines = result.split("\n");

        for (String line : lines) {

            if (line.contains(":")) {

                String[] parts =
                        line.split(":", 2);

                if (parts.length == 2) {

                    data.put(
                            parts[0]
                                    .trim()
                                    .toLowerCase(),
                            parts[1]
                                    .trim()
                    );
                }
            }
        }

        mealName =
                data.getOrDefault(
                        "food name",
                        "Unknown Meal"
                );

        binding.tvMealName.setText(
                mealName
        );

        try {

            String calString =
                    data.getOrDefault(
                                    "calories",
                                    "0"
                            )
                            .replaceAll(
                                    "[^0-9]",
                                    ""
                            );

            calories =
                    calString.isEmpty()
                            ? 0
                            : Integer.parseInt(
                            calString
                    );

        } catch (Exception e) {

            calories = 0;
        }

        binding.tvCalories.setText(
                calories + " kcal"
        );

        binding.tvProtein.setText(
                data.getOrDefault(
                        "protein",
                        "0 g"
                )
        );

        binding.tvCarbs.setText(
                data.getOrDefault(
                        "carbohydrates",
                        "0 g"
                )
        );

        binding.tvFat.setText(
                data.getOrDefault(
                        "fat",
                        "0 g"
                )
        );

        binding.tvFiber.setText(
                data.getOrDefault(
                        "fiber",
                        "0 g"
                )
        );

        binding.tvSugar.setText(
                data.getOrDefault(
                        "sugar",
                        "0 g"
                )
        );

        try {

            String scoreString =
                    data.getOrDefault(
                                    "health score",
                                    "88"
                            )
                            .replaceAll(
                                    "[^0-9]",
                                    ""
                            );

            healthScore =
                    scoreString.isEmpty()
                            ? 88
                            : Integer.parseInt(
                            scoreString
                    );

        } catch (Exception e) {

            healthScore = 88;
        }

        if (binding.tvHealthScore != null) {

            binding.tvHealthScore.setText(
                    healthScore + "/100"
            );
        }
    }

    private void setupListeners() {

        binding.btnBack.setOnClickListener(
                v -> finish()
        );

        binding.btnRescan.setOnClickListener(
                v -> finish()
        );

        binding.btnSave.setOnClickListener(
                v -> saveMeal()
        );
    }

    private void saveMeal() {

        try {

            // Save Calories
            int currentCalories =
                    prefs.getTodayCalories();

            prefs.setTodayCalories(
                    currentCalories + calories
            );

            // Auto Sync with Challenges
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirestoreManager.syncChallengeProgress(uid, "calories", currentCalories + calories);
            }

            // Save Health Score
            prefs.setHealthScore(
                    healthScore
            );

            Toast.makeText(
                    this,
                    mealName +
                            " added to today's nutrition log",
                    Toast.LENGTH_LONG
            ).show();

            finish();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to save meal",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
