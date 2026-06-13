package com.example.healthsync.frontend.data.repository;

import android.util.Log
import com.example.healthsync.frontend.data.local.*
import com.example.healthsync.frontend.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class HealthRepository(
    private val healthDao: HealthDao,
    private val prefs: PreferenceManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Profile Logic
    fun getUserProfile(): Flow<UserEntity?> = healthDao.getUserFlow()

    suspend fun syncProfileWithCloud() = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val user = UserEntity(
                    uid = userId,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 25,
                    gender = doc.getString("gender") ?: "Male",
                    heightCm = doc.getDouble("heightCm")?.toFloat() ?: 170f,
                    weightKg = doc.getDouble("weightKg")?.toFloat() ?: 70f,
                    points = doc.getLong("points")?.toInt() ?: 0,
                    lastSync = System.currentTimeMillis()
                )
                healthDao.insertUser(user)
                updateLocalPrefs(user)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLocalPrefs(user: UserEntity) {
        prefs.setUserName(user.name)
        prefs.setUserAge(user.age)
        prefs.setUserGender(user.gender)
        prefs.setUserHeight(user.heightCm)
        prefs.setUserWeight(user.weightKg)
        prefs.setHealthPoints(user.points)
        prefs.setSetupComplete(true)
    }

    // Water Tracking
    fun getTodayWater(): Flow<Int?> = healthDao.getTodayWaterFlow(getStartOfDay())

    suspend fun logWater(amountMl: Int) = withContext(Dispatchers.IO) {
        healthDao.insertWater(WaterLog(amountMl = amountMl, timestamp = System.currentTimeMillis()))
        val total = prefs.getWaterIntake() + amountMl
        prefs.setWaterIntake(total)
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            com.example.healthsync.frontend.firebase.FirestoreManager.syncChallengeProgress(userId, "water", total)
        }
    }

    // Sleep Tracking
    fun getTodaySleep(): Flow<SleepLog?> = healthDao.getTodaySleepFlow(getStartOfDay())
    fun getLatestSleepEntry(): Flow<SleepEntryEntity?> = healthDao.getLatestSleepEntryFlow()
    fun getWeeklySleepEntries(): Flow<List<SleepEntryEntity>> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return healthDao.getSleepEntriesSince(cal.timeInMillis)
    }

    suspend fun logSleepEntry(hours: Int, minutes: Int, score: Int) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = java.text.SimpleDateFormat("EEEE", Locale.getDefault())
        val dateStr = sdfDate.format(Date(now))
        val dayStr = sdfDay.format(Date(now))

        val existing = healthDao.getSleepEntryByDate(dateStr)
        val entry = if (existing != null) {
            existing.copy(sleepHours = hours, sleepMinutes = minutes, sleepScore = score, timestamp = now)
        } else {
            SleepEntryEntity(date = dateStr, dayOfWeek = dayStr, sleepHours = hours, sleepMinutes = minutes, sleepScore = score, timestamp = now)
        }

        healthDao.insertSleepEntry(entry)
        Log.d("SLEEP_SAVE", "Saved day: $dayStr, hours: $hours")

        // Update Prefs
        prefs.setSleepHours(if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h")
        prefs.setSleepScore(score)
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            com.example.healthsync.frontend.firebase.FirestoreManager.saveSleepEntry(entry)
            com.example.healthsync.frontend.firebase.FirestoreManager.syncChallengeProgress(userId, "sleep", hours)
        }
    }

    suspend fun logSleep(hours: Float, score: Int) = withContext(Dispatchers.IO) {
        val log = SleepLog(hours = hours, score = score, timestamp = System.currentTimeMillis())
        healthDao.insertSleep(log)
        
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        prefs.setSleepHours(if (m > 0) "${h}h ${m}m" else "${h}h")
        prefs.setSleepScore(score)
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Deprecated path, using entry logic above
            com.example.healthsync.frontend.firebase.FirestoreManager.syncChallengeProgress(userId, "sleep", h)
        }
    }

    // Stress & Mood
    fun getTodayStress(): Flow<StressLog?> = healthDao.getTodayStressFlow(getStartOfDay())
    fun getMoodHistory(): Flow<List<MoodLog>> = healthDao.getMoodHistory()

    suspend fun logMood(mood: String, note: String) = withContext(Dispatchers.IO) {
        val score = com.example.healthsync.frontend.utils.WellnessAnalyticsEngine.mapMoodToStress(mood)
        val level = if (score < 30) "Low" else if (score < 60) "Medium" else "High"
        
        val log = MoodLog(mood = mood, note = note, timestamp = System.currentTimeMillis())
        healthDao.insertMood(log)
        
        val stressLog = StressLog(score = score, level = level, timestamp = System.currentTimeMillis())
        healthDao.insertStress(stressLog)
        
        prefs.setStressScore(score)
        prefs.setStressLevel(level)
        prefs.setLastMood(mood)
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            com.example.healthsync.frontend.firebase.FirestoreManager.saveMoodLog(log)
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
