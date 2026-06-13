package com.example.healthsync.backend.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(step: StepEntity)

    @Query("SELECT * FROM step_history WHERE date = :date LIMIT 1")
    suspend fun getStepsForDate(date: String): StepEntity?

    @Query("SELECT * FROM step_history ORDER BY timestamp DESC")
    fun getAllStepsFlow(): Flow<List<StepEntity>>

    @Query("SELECT SUM(steps) FROM step_history")
    suspend fun getTotalSteps(): Int?
}
