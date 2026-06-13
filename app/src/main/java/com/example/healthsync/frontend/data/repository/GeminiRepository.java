package com.example.healthsync.frontend.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.healthsync.BuildConfig;
import com.example.healthsync.frontend.data.local.MealDatabase;
import com.example.healthsync.frontend.data.local.MealPlanDao;
import com.example.healthsync.frontend.data.local.MealPlanEntity;
import com.example.healthsync.frontend.data.model.MealPlanModel;
import com.example.healthsync.frontend.data.remote.GeminiApiService;
import com.example.healthsync.frontend.firebase.FirestoreManager;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiRepository {
    private static final String TAG_REQ = "GEMINI_REQUEST";
    private static final String TAG_RES = "GEMINI_RESPONSE";
    private static final String TAG_ERR = "GEMINI_ERROR";

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private final GeminiApiService apiService;
    private final MealPlanDao mealPlanDao;
    private final Gson gson;
    private final ExecutorService executor;

    public GeminiRepository(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(GeminiApiService.class);
        this.mealPlanDao = MealDatabase.Companion.getDatabase(context).mealPlanDao();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getMealPlan(String prompt, boolean forceRefresh, Callback<MealPlanModel> callback) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        executor.execute(() -> {
            if (!forceRefresh) {
                MealPlanEntity cached = mealPlanDao.getMealPlanByDate(today);
                if (cached != null) {
                    try {
                        MealPlanModel model = gson.fromJson(cached.jsonContent, MealPlanModel.class);
                        if (model != null) {
                            callback.onSuccess(model);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG_ERR, "Cache parse error", e);
                    }
                }
            }

            // Authentication Setup
            String apiKey = BuildConfig.GEMINI_API_KEY.trim();
            if (apiKey.isEmpty()) {
                Log.e(TAG_ERR, "Gemini API key not configured. Set GEMINI_API_KEY in local.properties.");
                callback.onError("AI service not configured. Please contact support.");
                return;
            }

            // Try with primary model then fallback
            boolean success = tryFetchWithModel("gemini-2.5-flash", apiKey, prompt, today, callback);
            if (!success) {
                Log.w(TAG_REQ, "Primary model failed or unavailable. Falling back to gemini-2.0-flash.");
                tryFetchWithModel("gemini-2.0-flash", apiKey, prompt, today, callback);
            }
        });
    }

    private boolean tryFetchWithModel(String modelName, String apiKey, String prompt, String date, Callback<MealPlanModel> callback) {
        int maxRetries = 3;
        int[] sleepTimes = {0, 2000, 4000}; // Wait times before attempts

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    Log.d(TAG_REQ, "Retry attempt " + attempt + " for model " + modelName);
                    Thread.sleep(sleepTimes[attempt - 1]);
                }

                Log.d(TAG_REQ, "Model: " + modelName);
                Log.d(TAG_REQ, "Prompt Sample: " + (prompt.length() > 100 ? prompt.substring(0, 100) : prompt));
                
                GeminiApiService.GeminiRequest request = new GeminiApiService.GeminiRequest(prompt);
                retrofit2.Call<GeminiApiService.GeminiResponse> call = apiService.generateMealPlan(modelName, apiKey, request);
                
                Log.d(TAG_REQ, "URL: " + call.request().url());

                Response<GeminiApiService.GeminiResponse> response = call.execute();
                Log.d(TAG_RES, "Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String rawJson = response.body().getResponseText();
                    if (rawJson != null) {
                        rawJson = rawJson.replace("```json", "").replace("```", "").trim();
                        MealPlanModel model = gson.fromJson(rawJson, MealPlanModel.class);
                        
                        if (model != null) {
                            MealPlanEntity entity = new MealPlanEntity(
                                    date, 
                                    rawJson, 
                                    model.totalCalories, 
                                    model.totalProtein, 
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
                            );
                            mealPlanDao.insertMealPlan(entity);
                            FirestoreManager.saveMealPlan(entity, model);
                            
                            callback.onSuccess(model);
                            return true;
                        }
                    }
                    Log.e(TAG_ERR, "Malformed or empty response from AI");
                } else {
                    String errorBody = "No error body";
                    if (response.errorBody() != null) {
                        try { errorBody = response.errorBody().string(); } catch (Exception ignored) {}
                    }
                    Log.e(TAG_ERR, "Status: " + response.code() + " | Body: " + errorBody);

                    // If it's a 404 or 503, the model might be unavailable or busy, try fallback
                    if (response.code() == 404 || response.code() == 503) {
                        if (attempt == maxRetries) return false; // Trigger fallback
                        continue; // Retry this model
                    }
                    
                    if (attempt == maxRetries) {
                        String message = getErrorMessage(response.code());
                        callback.onError(message);
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG_ERR, "Attempt " + attempt + " failed: " + e.getMessage(), e);
                if (attempt == maxRetries) {
                    callback.onError("Network error: " + e.getMessage());
                    return true;
                }
            }
        }
        return false;
    }

    private String getErrorMessage(int code) {
        switch (code) {
            case 400: return "Bad Request: Check your health profile data.";
            case 401: return "Unauthorized: Invalid AI configuration.";
            case 403: return "Forbidden: Access denied to AI model.";
            case 404: return "AI model not found. Trying fallback...";
            case 429: return "Too many requests. Please wait a moment.";
            case 500: return "AI Server Error. Please try again later.";
            case 503: return "AI service is temporarily busy. Please try again.";
            default: return "Error " + code + ": Unable to generate meal plan.";
        }
    }
}
