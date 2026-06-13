package com.example.healthsync.backend.data.repository

import android.content.Context
import com.example.healthsync.backend.data.local.StepDao
import com.example.healthsync.backend.data.local.StepEntity
import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class StepRepository(
    private val stepDao: StepDao,
    private val prefs: PreferenceManager
) {
    private val auth = FirebaseAuth.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getAllSteps(): Flow<List<StepEntity>> = stepDao.getAllStepsFlow()

    suspend fun updateSteps(steps: Int) {
        val today = sdf.format(Date())
        val entity = StepEntity(today, steps, System.currentTimeMillis())
        stepDao.insertSteps(entity)
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            FirestoreManager.syncStepData(userId, steps)
        }
    }

    fun updateStepsSync(steps: Int) {
        val today = sdf.format(Date())
        val entity = StepEntity(today, steps, System.currentTimeMillis())
        // Run on IO thread
        kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            stepDao.insertSteps(entity)
            val userId = auth.currentUser?.uid
            if (userId != null) {
                FirestoreManager.syncStepData(userId, steps)
            }
        }
    }

    suspend fun getTodaySteps(): Int {
        val today = sdf.format(Date())
        return stepDao.getStepsForDate(today)?.steps ?: 0
    }
}
