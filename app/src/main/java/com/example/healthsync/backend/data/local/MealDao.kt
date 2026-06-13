package com.example.healthsync.backend.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMealSync(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE timestamp >= :startOfDay")
    fun getMealsToday(startOfDay: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE timestamp >= :startOfDay")
    fun getMealsTodayList(startOfDay: Long): List<MealEntity>

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
}
