package com.example.healthsync.backend.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.healthsync.BuildConfig;
import com.example.healthsync.backend.data.local.MealDatabase;
import com.example.healthsync.backend.data.local.MealPlanDao;
import com.example.healthsync.backend.data.local.MealPlanEntity;
import com.example.healthsync.backend.data.model.MealPlanModel;
import com.example.healthsync.backend.data.remote.GeminiApiService;
import com.example.healthsync.backend.firebase.FirestoreManager;
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

            boolean success = tryFetchWithModel("gemini-2.5-flash", apiKey, prompt, today, callback);
            if (!success) {
                Log.w(TAG_REQ, "Primary model failed or unavailable. Falling back to gemini-2.0-flash.");
                tryFetchWithModel("gemini-2.0-flash", apiKey, prompt, today, callback);
            }
        });
    }

    private boolean tryFetchWithModel(String modelName, String apiKey, String prompt, String date, Callback<MealPlanModel> callback) {
        int maxRetries = 3;
        int[] sleepTimes = {0, 2000, 4000};

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) Thread.sleep(sleepTimes[attempt - 1]);

                GeminiApiService.GeminiRequest request = new GeminiApiService.GeminiRequest(prompt);
                retrofit2.Call<GeminiApiService.GeminiResponse> call = apiService.generateMealPlan(modelName, apiKey, request);
                
                Response<GeminiApiService.GeminiResponse> response = call.execute();

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
                } else {
                    if (response.code() == 404 || response.code() == 503) {
                        if (attempt == maxRetries) return false;
                        continue;
                    }
                    
                    if (attempt == maxRetries) {
                        callback.onError("Error " + response.code());
                        return true; 
                    }
                }
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    callback.onError("Network error: " + e.getMessage());
                    return true;
                }
            }
        }
        return false;
    }
}
