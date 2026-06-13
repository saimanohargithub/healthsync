package com.example.healthsync.frontend.data.local;

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUser(): UserEntity?

    // Water
    @Insert
    suspend fun insertWater(log: WaterLog)

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE timestamp >= :startOfDay")
    fun getTodayWaterFlow(startOfDay: Long): Flow<Int?>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLog>>

    // Sleep (Legacy)
    @Insert
    suspend fun insertSleep(log: SleepLog)

    @Query("SELECT * FROM sleep_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC LIMIT 1")
    fun getTodaySleepFlow(startOfDay: Long): Flow<SleepLog?>

    @Query("SELECT * FROM sleep_logs ORDER BY timestamp DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    // Sleep (New SleepEntryEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepEntry(entry: SleepEntryEntity)

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    suspend fun getSleepEntryByDate(date: String): SleepEntryEntity?

    @Query("SELECT * FROM sleep_entries ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSleepEntryFlow(): Flow<SleepEntryEntity?>

    @Query("SELECT * FROM sleep_entries WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getSleepEntriesSince(since: Long): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getSleepEntriesSinceList(since: Long): List<SleepEntryEntity>

    // Stress & Mood
    @Insert
    suspend fun insertStress(log: StressLog)

    @Insert
    suspend fun insertMood(log: MoodLog)

    @Query("SELECT * FROM stress_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC LIMIT 1")
    fun getTodayStressFlow(startOfDay: Long): Flow<StressLog?>

    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getMoodHistory(): Flow<List<MoodLog>>
}
