package com.example.healthsync.frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "HealthSyncPrefs";

    private static final String KEY_IS_SETUP_COMPLETE = "is_setup_complete";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
    private static final String KEY_USER_GENDER = "user_gender";
    private static final String KEY_USER_HEIGHT = "user_height";
    private static final String KEY_USER_WEIGHT = "user_weight";

    private static final String KEY_WATER_INTAKE = "water_intake";
    private static final String KEY_WATER_GOAL = "water_goal";

    private static final String KEY_LAST_MOOD = "last_mood";
    private static final String KEY_STRESS_LEVEL = "stress_level";
    private static final String KEY_STRESS_SCORE = "stress_score";

    private static final String KEY_TODAY_CALORIES = "today_calories";
    private static final String KEY_TODAY_PROTEIN = "today_protein";
    private static final String KEY_TODAY_CARBS = "today_carbs";
    private static final String KEY_TODAY_FAT = "today_fat";
    private static final String KEY_TODAY_SODIUM = "today_sodium";
    private static final String KEY_TODAY_FIBER = "today_fiber";
    private static final String KEY_TODAY_SUGAR = "today_sugar";

    private static final String KEY_SLEEP_HOURS = "sleep_hours";
    private static final String KEY_SLEEP_SCORE = "sleep_score";

    private static final String KEY_HEALTH_SCORE = "health_score";
    private static final String KEY_HEALTH_POINTS = "health_points";
    private static final String KEY_STEP_OFFSET = "step_offset";
    private static final String KEY_LAST_STEP_COUNT = "last_step_count";
    private static final String KEY_STEP_DATE = "step_date";

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    // Setup

    public void setSetupComplete(boolean isComplete) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_SETUP_COMPLETE, isComplete)
                .apply();
    }

    public boolean isSetupComplete() {
        return sharedPreferences.getBoolean(
                KEY_IS_SETUP_COMPLETE,
                false
        );
    }

    // User

    public void setUserName(String name) {
        sharedPreferences.edit()
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(
                KEY_USER_NAME,
                "Alex Rivera"
        );
    }
// User Details

    public void setUserAge(int age) {
        sharedPreferences.edit()
                .putInt(KEY_USER_AGE, age)
                .apply();
    }

    public int getUserAge() {
        return sharedPreferences.getInt(
                KEY_USER_AGE,
                0
        );
    }
    public void setUserGender(String gender) {
        sharedPreferences.edit()
                .putString(KEY_USER_GENDER, gender)
                .apply();
    }

    public String getUserGender() {
        return sharedPreferences.getString(
                KEY_USER_GENDER,
                ""
        );
    }
    public void setUserHeight(float height) {
        sharedPreferences.edit()
                .putFloat(KEY_USER_HEIGHT, height)
                .apply();
    }

    public float getUserHeight() {
        return sharedPreferences.getFloat(
                KEY_USER_HEIGHT,
                0f
        );
    }

    public void setUserWeight(float weight) {
        sharedPreferences.edit()
                .putFloat(KEY_USER_WEIGHT, weight)
                .apply();
    }

    public float getUserWeight() {
        return sharedPreferences.getFloat(
                KEY_USER_WEIGHT,
                0f
        );
    }
    // Water

    public void setWaterIntake(int ml) {
        sharedPreferences.edit()
                .putInt(KEY_WATER_INTAKE, ml)
                .apply();
    }

    public int getWaterIntake() {
        return sharedPreferences.getInt(
                KEY_WATER_INTAKE,
                0
        );
    }

    public void setWaterGoal(int ml) {
        sharedPreferences.edit()
                .putInt(KEY_WATER_GOAL, ml)
                .apply();
    }

    public int getWaterGoal() {
        return sharedPreferences.getInt(
                KEY_WATER_GOAL,
                3000
        );
    }

    // Calories

    public void setTodayCalories(int kcal) {
        sharedPreferences.edit()
                .putInt(KEY_TODAY_CALORIES, kcal)
                .apply();
    }

    public int getTodayCalories() {
        return sharedPreferences.getInt(
                KEY_TODAY_CALORIES,
                0
        );
    }

    public void setTodayProtein(float grams) {
        sharedPreferences.edit().putFloat(KEY_TODAY_PROTEIN, grams).apply();
    }

    public float getTodayProtein() {
        return sharedPreferences.getFloat(KEY_TODAY_PROTEIN, 0f);
    }

    public void setTodayCarbs(float grams) {
        sharedPreferences.edit().putFloat(KEY_TODAY_CARBS, grams).apply();
    }

    public float getTodayCarbs() {
        return sharedPreferences.getFloat(KEY_TODAY_CARBS, 0f);
    }

    public void setTodayFat(float grams) {
        sharedPreferences.edit().putFloat(KEY_TODAY_FAT, grams).apply();
    }

    public float getTodayFat() {
        return sharedPreferences.getFloat(KEY_TODAY_FAT, 0f);
    }

    public void setTodaySodium(float mg) {
        sharedPreferences.edit().putFloat(KEY_TODAY_SODIUM, mg).apply();
    }

    public float getTodaySodium() {
        return sharedPreferences.getFloat(KEY_TODAY_SODIUM, 0f);
    }

    public void setTodayFiber(float fiber) {
        sharedPreferences.edit().putFloat(KEY_TODAY_FIBER, fiber).apply();
    }

    public float getTodayFiber() {
        return sharedPreferences.getFloat(KEY_TODAY_FIBER, 0f);
    }

    public void setTodaySugar(float sugar) {
        sharedPreferences.edit().putFloat(KEY_TODAY_SUGAR, sugar).apply();
    }

    public float getTodaySugar() {
        return sharedPreferences.getFloat(KEY_TODAY_SUGAR, 0f);
    }

    // Sleep

    public void setSleepHours(String hours) {
        sharedPreferences.edit()
                .putString(KEY_SLEEP_HOURS, hours)
                .apply();
    }

    public String getSleepHours() {
        return sharedPreferences.getString(
                KEY_SLEEP_HOURS,
                "7h 20m"
        );
    }

    public void setSleepScore(int score) {
        sharedPreferences.edit()
                .putInt(KEY_SLEEP_SCORE, score)
                .apply();
    }

    public int getSleepScore() {
        return sharedPreferences.getInt(
                KEY_SLEEP_SCORE,
                82
        );
    }

    // Stress

    public void setLastMood(String mood) {
        sharedPreferences.edit()
                .putString(KEY_LAST_MOOD, mood)
                .apply();
    }

    public String getLastMood() {
        return sharedPreferences.getString(
                KEY_LAST_MOOD,
                "Calm"
        );
    }

    public void setStressLevel(String level) {
        sharedPreferences.edit()
                .putString(KEY_STRESS_LEVEL, level)
                .apply();
    }

    public String getStressLevel() {
        return sharedPreferences.getString(
                KEY_STRESS_LEVEL,
                "Calm"
        );
    }

    public void setStressScore(int score) {
        sharedPreferences.edit()
                .putInt(KEY_STRESS_SCORE, score)
                .apply();
    }

    public int getStressScore() {
        return sharedPreferences.getInt(
                KEY_STRESS_SCORE,
                24
        );
    }

    // Health Score

    public void setHealthScore(int score) {
        sharedPreferences.edit()
                .putInt(KEY_HEALTH_SCORE, score)
                .apply();
    }

    public int getHealthScore() {
        return sharedPreferences.getInt(
                KEY_HEALTH_SCORE,
                88
        );
    }

    // Health Points

    public void setHealthPoints(int points) {
        sharedPreferences.edit()
                .putInt(KEY_HEALTH_POINTS, points)
                .apply();
    }

    public int getHealthPoints() {
        return sharedPreferences.getInt(
                KEY_HEALTH_POINTS,
                0
        );
    }

    public void setStepOffset(int offset) {
        sharedPreferences.edit().putInt(KEY_STEP_OFFSET, offset).apply();
    }

    public int getStepOffset() {
        return sharedPreferences.getInt(KEY_STEP_OFFSET, 0);
    }

    public void setLastStepCount(int count) {
        sharedPreferences.edit().putInt(KEY_LAST_STEP_COUNT, count).apply();
    }

    public int getLastStepCount() {
        return sharedPreferences.getInt(KEY_LAST_STEP_COUNT, 0);
    }

    public void setStepDate(String date) {
        sharedPreferences.edit().putString(KEY_STEP_DATE, date).apply();
    }

    public String getStepDate() {
        return sharedPreferences.getString(KEY_STEP_DATE, "");
    }

    // Clear

    public void clear() {
        sharedPreferences.edit()
                .clear()
                .apply();
    }
}
