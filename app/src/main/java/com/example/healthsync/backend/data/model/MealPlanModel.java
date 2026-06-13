package com.example.healthsync.backend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MealPlanModel {
    @SerializedName("breakfast")
    public Meal breakfast;
    
    @SerializedName("lunch")
    public Meal lunch;
    
    @SerializedName("dinner")
    public Meal dinner;
    
    @SerializedName("snack")
    public Meal snack;
    
    @SerializedName("total_daily_calories")
    public int totalCalories;
    
    @SerializedName("total_protein")
    public int totalProtein;
    
    @SerializedName("health_advice")
    public String healthAdvice;

    public static class Meal {
        @SerializedName("meal_name")
        public String mealName;
        
        @SerializedName("food_items")
        public List<String> foodItems;
        
        @SerializedName("calories")
        public int calories;
        
        @SerializedName("protein")
        public int protein;
        
        @SerializedName("carbs")
        public int carbs;
        
        @SerializedName("fat")
        public int fat;
        
        @SerializedName("scheduled_time")
        public String scheduledTime;
        
        public boolean isCompleted = false;
    }
}
