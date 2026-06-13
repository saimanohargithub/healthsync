package com.example.healthsync.frontend.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMealPlan(MealPlanEntity mealPlan);

    @Query("SELECT * FROM meal_plans WHERE date = :date LIMIT 1")
    MealPlanEntity getMealPlanByDate(String date);

    @Query("SELECT * FROM meal_plans ORDER BY date DESC LIMIT 1")
    MealPlanEntity getLatestMealPlan();
}
