package com.example.healthsync.backend.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.healthsync.backend.data.local.MealDao;
import com.example.healthsync.backend.data.local.MealDatabase;
import com.example.healthsync.backend.data.local.MealEntity;
import com.example.healthsync.backend.data.model.MealPlanModel;
import com.example.healthsync.backend.data.repository.GeminiRepository;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.utils.UserDataHolder;
import com.example.healthsync.frontend.utils.PreferenceManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealPlanViewModel extends AndroidViewModel {
    private final GeminiRepository repository;
    private final PreferenceManager prefs;
    private final MealDao mealDao;
    
    private final MutableLiveData<MealPlanModel> mealPlan = new MutableLiveData<>();
    private final MutableLiveData<List<MealEntity>> todayMeals = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MealPlanViewModel(@NonNull Application application) {
        super(application);
        this.repository = new GeminiRepository(application);
        this.prefs = new PreferenceManager(application);
        this.mealDao = MealDatabase.Companion.getDatabase(application).mealDao();
    }

    public LiveData<MealPlanModel> getMealPlan() { return mealPlan; }
    public LiveData<List<MealEntity>> getTodayMeals() { return todayMeals; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchTodayMeals() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        new Thread(() -> {
            List<MealEntity> meals = mealDao.getMealsTodayList(startOfDay);
            todayMeals.postValue(meals);
        }).start();
    }

    public void fetchMealPlan(boolean forceRefresh) {
        isLoading.setValue(true);
        String prompt = generatePrompt();
        
        repository.getMealPlan(prompt, forceRefresh, new GeminiRepository.Callback<MealPlanModel>() {
            @Override
            public void onSuccess(MealPlanModel result) {
                isLoading.postValue(false);
                mealPlan.postValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    private String generatePrompt() {
        UserProfile profile = UserDataHolder.userProfile;
        int age = prefs.getUserAge();
        String gender = prefs.getUserGender();
        float weight = prefs.getUserWeight();
        float height = prefs.getUserHeight();
        
        float heightM = height / 100f;
        float bmi = (heightM > 0) ? weight / (heightM * heightM) : 0;
        
        String goal = profile != null && profile.goal != null ? profile.goal : "Maintain Weight";
        String activity = profile != null && profile.activityLevel != null ? profile.activityLevel : "Moderate";
        List<String> medical = profile != null && profile.medicalConditions != null ? profile.medicalConditions : List.of();

        int calTarget = 2000;
        if (goal.contains("Loss")) calTarget = 1800;
        else if (goal.contains("Gain")) calTarget = 2500;

        return String.format(Locale.getDefault(), 
                "Generate a personalized 1-day meal plan in JSON.\n" +
                "Profile: %dyo %s, %.1fkg, %.1fcm, BMI: %.1f. Goal: %s. Activity: %s. Medical: %s. Target: %d kcal.\n" +
                "Output JSON format:\n" +
                "{\n" +
                "  \"breakfast\": { \"meal_name\": \"\", \"food_items\": [], \"calories\": 0, \"protein\": 0, \"carbs\": 0, \"fat\": 0, \"scheduled_time\": \"8:00 AM\" },\n" +
                "  \"lunch\": { \"meal_name\": \"\", \"food_items\": [], \"calories\": 0, \"protein\": 0, \"carbs\": 0, \"fat\": 0, \"scheduled_time\": \"1:00 PM\" },\n" +
                "  \"dinner\": { \"meal_name\": \"\", \"food_items\": [], \"calories\": 0, \"protein\": 0, \"carbs\": 0, \"fat\": 0, \"scheduled_time\": \"7:30 PM\" },\n" +
                "  \"snack\": { \"meal_name\": \"\", \"food_items\": [], \"calories\": 0, \"protein\": 0, \"carbs\": 0, \"fat\": 0, \"scheduled_time\": \"4:00 PM\" },\n" +
                "  \"total_daily_calories\": 0, \"total_protein\": 0, \"health_advice\": \"\"\n" +
                "}",
                age, gender, weight, height, bmi, goal, activity, medical.toString(), calTarget
        );
    }

    public void completeMeal(MealPlanModel.Meal meal) {
        if (meal == null || meal.isCompleted) return;
        
        meal.isCompleted = true;
        
        prefs.setTodayCalories(prefs.getTodayCalories() + meal.calories);
        prefs.setTodayProtein(prefs.getTodayProtein() + (float) meal.protein);
        prefs.setTodayCarbs(prefs.getTodayCarbs() + (float) meal.carbs);
        prefs.setTodayFat(prefs.getTodayFat() + (float) meal.fat);
        
        new Thread(() -> {
            MealEntity entity = new MealEntity(
                0,
                meal.mealName,
                "",
                System.currentTimeMillis(),
                (double) meal.calories,
                (double) meal.protein,
                (double) meal.carbs,
                (double) meal.fat,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                "AI_PLAN"
            );
            mealDao.insertMealSync(entity);
            fetchTodayMeals();
        }).start();

        mealPlan.setValue(mealPlan.getValue());
    }
}
