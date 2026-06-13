package com.example.healthsync.backend.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_plans")
public class MealPlanEntity {
    @PrimaryKey
    @NonNull
    public String date;
    
    public String jsonContent;
    
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
