package com.example.healthsync.backend.engines

import kotlin.math.max
import kotlin.math.min

object HealthScoreEngine {
    fun calculateScore(
        waterMl: Int,
        waterGoal: Int,
        sleepHours: Float,
        stressScore: Int,
        calories: Int,
        protein: Float
    ): Int {
        var score = 100
        
        if (waterMl < waterGoal) score -= 15 * (1 - waterMl.toFloat() / waterGoal).toInt().coerceIn(0, 1)
        if (sleepHours < 7) score -= 10
        if (sleepHours < 5) score -= 10
        if (stressScore > 50) score -= 15
        if (stressScore > 80) score -= 10
        if (calories > 2500) score -= 10
        if (protein < 50) score -= 10

        return score.coerceIn(0, 100)
    }
}

object RiskCalculator {
    data class RiskResult(
        val diabetesRisk: Int,
        val hypertensionRisk: Int,
        val obesityRisk: Int,
        val overallStatus: String,
        val recommendation: String
    )

    fun calculateRisks(
        age: Int,
        bmi: Float,
        stressScore: Int,
        sodium: Float,
        sleepHours: Float,
        waterMl: Int,
        waterGoal: Int
    ): RiskResult {
        val diabetes = (age / 2.0 + bmi * 1.5).toInt().coerceIn(0, 100)
        val sodiumImpact = if (sodium > 2300) 20 else 0
        val hypertension = (stressScore / 2.0 + age / 3.0 + sodiumImpact).toInt().coerceIn(0, 100)
        val obesity = if (bmi < 25) 10 else if (bmi < 30) 45 else 85
        
        val overallScore = 100 - (diabetes + hypertension + obesity) / 3
        val status = if (overallScore > 80) "Excellent" else if (overallScore > 60) "Good" else "High Risk"
        
        val rec = when {
            sodium > 2300 -> "Reduce salt intake to manage hypertension risk."
            waterMl < waterGoal -> "Increase hydration to improve metabolic health."
            bmi > 25 -> "Focus on weight management and balanced nutrition."
            sleepHours < 6 -> "Improve sleep duration for better recovery."
            else -> "Maintain your consistent healthy lifestyle."
        }

        return RiskResult(diabetes, hypertension, obesity, status, rec)
    }
}
