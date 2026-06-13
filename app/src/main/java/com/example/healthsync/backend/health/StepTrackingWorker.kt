package com.example.healthsync.backend.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthsync.backend.data.local.MealDatabase
import com.example.healthsync.backend.data.repository.StepRepository
import com.example.healthsync.frontend.utils.PreferenceManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

class StepTrackingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return Result.failure()
        
        val database = MealDatabase.getDatabase(applicationContext)
        val prefs = PreferenceManager(applicationContext)
        val stepRepository = StepRepository(database.stepDao(), prefs)

        val deferred = CompletableDeferred<Int>()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    deferred.complete(event.values[0].toInt())
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        return try {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            val totalSteps = withTimeoutOrNull(5000) { deferred.await() }
            sensorManager.unregisterListener(listener)

            if (totalSteps != null) {
                val dailySteps = calculateDailySteps(totalSteps, prefs)
                stepRepository.updateSteps(dailySteps)
                Log.d("STEP_SYNC", "Worker synced $dailySteps steps")
                Result.success()
            } else {
                Log.w("STEP_SYNC", "Worker timed out waiting for sensor")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("STEP_SYNC", "Worker error", e)
            Result.retry()
        }
    }

    private fun calculateDailySteps(totalSteps: Int, prefs: PreferenceManager): Int {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())
        if (today != prefs.getStepDate()) {
            prefs.setStepOffset(totalSteps)
            prefs.setStepDate(today)
            return 0
        }
        var dailySteps = totalSteps - prefs.getStepOffset()
        if (dailySteps < 0) {
            prefs.setStepOffset(totalSteps)
            dailySteps = 0
        }
        return dailySteps
    }
}
