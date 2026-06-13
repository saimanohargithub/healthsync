package com.example.healthsync.frontend.utils;

import com.example.healthsync.frontend.data.local.SleepEntryEntity
import com.example.healthsync.frontend.data.local.MoodLog
import java.util.*
import kotlin.math.roundToInt

object SleepAnalyticsEngine {
    data class WeeklyStats(
        val averageHours: Float,
        val bestDay: String,
        val worstDay: String,
        val trend: String,
        val chartValues: FloatArray,
        val dayLabels: Array<String>
    )

    fun calculateWeeklyStats(logs: List<SleepEntryEntity>): WeeklyStats {
        val hoursPerDay = FloatArray(7)
        val dayLabels = Array(7) { "" }
        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        // Initialize last 7 days including today
        for (i in 0..6) {
            val d = Calendar.getInstance()
            d.add(Calendar.DAY_OF_YEAR, - (6 - i))
            dayLabels[i] = daysOfWeek[d.get(Calendar.DAY_OF_WEEK) - 1]
            
            val dateStr = formatDate(d.timeInMillis)
            val dayLog = logs.find { it.date == dateStr }
            hoursPerDay[i] = if (dayLog != null) dayLog.sleepHours + (dayLog.sleepMinutes / 60f) else 0f
        }

        val activeDays = hoursPerDay.filter { it > 0 }
        val avg = if (activeDays.isNotEmpty()) activeDays.average().toFloat() else 0f
        
        var bestIdx = 0
        var worstIdx = 0
        for (i in hoursPerDay.indices) {
            if (hoursPerDay[i] > hoursPerDay[bestIdx]) bestIdx = i
            if (hoursPerDay[i] > 0 && (hoursPerDay[i] < hoursPerDay[worstIdx] || hoursPerDay[worstIdx] == 0f)) worstIdx = i
        }

        // Trend calculation (compare first half of week to second half)
        val firstHalf = hoursPerDay.slice(0..2).average()
        val secondHalf = hoursPerDay.slice(4..6).average()
        val trend = when {
            secondHalf > firstHalf + 0.5 -> "Improving"
            secondHalf < firstHalf - 0.5 -> "Declining"
            else -> "Stable"
        }

        return WeeklyStats(
            averageHours = avg,
            bestDay = if (hoursPerDay[bestIdx] > 0) dayLabels[bestIdx] else "N/A",
            worstDay = if (hoursPerDay[worstIdx] > 0) dayLabels[worstIdx] else "N/A",
            trend = trend,
            chartValues = hoursPerDay,
            dayLabels = dayLabels
        )
    }

    private fun formatDate(ts: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(ts))
    }
}

object WellnessAnalyticsEngine {
    data class WellnessReport(
        val status: String,
        val insight: String,
        val wellnessScore: Int
    )

    fun generateReport(sleepScore: Int, stressScore: Int): WellnessReport {
        // Wellness score: Higher is better. Stress is inverted (100 - stress)
        val score = (sleepScore + (100 - stressScore)) / 2
        
        val status = when {
            score >= 85 -> "Excellent"
            score >= 70 -> "Good"
            score >= 50 -> "Average"
            else -> "Needs Improvement"
        }

        val insight = when {
            stressScore > 70 -> "High stress detected. Deep breathing exercises can help lower cortisol."
            sleepScore < 60 -> "You're consistently undersleeping. Aim for 7-8 hours to improve focus."
            score >= 85 -> "Great job! Your habits are supporting optimal recovery."
            else -> "Maintaining a consistent sleep schedule will boost your mood and energy."
        }

        return WellnessReport(status, insight, score)
    }

    fun mapMoodToStress(mood: String): Int {
        return when (mood.lowercase()) {
            "happy" -> 10
            "calm" -> 20
            "neutral" -> 50
            "stressed" -> 75
            "sad" -> 85
            "angry" -> 95
            else -> 50
        }
    }
}
