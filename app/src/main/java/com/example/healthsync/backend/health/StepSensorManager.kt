package com.example.healthsync.backend.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.healthsync.frontend.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class StepSensorManager(private val context: Context, private val onStepsChanged: (Int) -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = PreferenceManager(context)
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun start() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("STEP_SENSOR", "Step sensor registered")
        } else {
            Log.e("STEP_SENSOR", "Step counter sensor not available")
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("STEP_SENSOR", "Step sensor unregistered")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()
            Log.d("STEP_SENSOR", "Sensor update: $totalStepsSinceBoot")
            handleStepUpdate(totalStepsSinceBoot)
        }
    }

    private fun handleStepUpdate(totalSteps: Int) {
        val today = sdf.format(Date())
        val lastSavedDate = prefs.stepDate

        if (today != lastSavedDate) {
            // New day: set current total as offset
            prefs.setStepOffset(totalSteps)
            prefs.setStepDate(today)
            Log.d("STEP_SENSOR", "New day detected. Resetting offset to $totalSteps")
        }

        val offset = prefs.stepOffset
        
        // If device rebooted, totalSteps might be less than offset
        var dailySteps = totalSteps - offset
        if (dailySteps < 0) {
            // Reboot detected
            prefs.setStepOffset(totalSteps)
            dailySteps = 0
            Log.d("STEP_SENSOR", "Reboot detected. Resetting offset to $totalSteps")
        }

        onStepsChanged(dailySteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
