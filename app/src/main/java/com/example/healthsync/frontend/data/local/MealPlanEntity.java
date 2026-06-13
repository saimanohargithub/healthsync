package com.example.healthsync.frontend.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_plans")
public class MealPlanEntity {
    @PrimaryKey
    @NonNull
    public String date; // YYYY-MM-DD
    
    public String jsonContent; // Store the whole MealPlanModel as JSON
    
    public int totalCalories;
    public int totalProtein;
    public String generatedAt;

    public MealPlanEntity(@NonNull String date, String jsonContent, int totalCalories, int totalProtein, String generatedAt) {
        this.date = date;
        this.jsonContent = jsonContent;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.generatedAt = generatedAt;
    }
}
