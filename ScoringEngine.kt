package com.brainrush.app.domain.scoring

data class AnswerReward(
    val basePoints: Int,
    val speedBonus: Int,
    val comboBonus: Int,
    val totalPointsEarned: Int,
    val xpEarned: Int,
    val newCombo: Int
)

class ScoringEngine {
    companion object {
        const val BASE_XP_PER_CORRECT = 100
        const val BASE_SCORE_PER_CORRECT = 100
        const val FAST_ANSWER_THRESHOLD_MS = 3_000L
        const val FAST_ANSWER_BONUS = 50
        const val COMBO_5_BONUS = 100
        const val COMBO_10_BONUS = 250
        const val XP_PER_LEVEL = 1_000L
    }

    fun evaluateCorrectAnswer(currentCombo: Int, responseTimeMs: Long): AnswerReward {
        val newCombo = currentCombo + 1
        val speedBonus = if (responseTimeMs in 0..FAST_ANSWER_THRESHOLD_MS) FAST_ANSWER_BONUS else 0
        val comboBonus = when {
            newCombo >= 10 && newCombo % 10 == 0 -> COMBO_10_BONUS
            newCombo >= 5 && newCombo % 5 == 0 -> COMBO_5_BONUS
            else -> 0
        }
        val totalPoints = BASE_SCORE_PER_CORRECT + speedBonus + comboBonus
        val xpEarned = BASE_XP_PER_CORRECT + (speedBonus / 2) + (comboBonus / 2)
        return AnswerReward(BASE_SCORE_PER_CORRECT, speedBonus, comboBonus, totalPoints, xpEarned, newCombo)
    }

    fun evaluateWrongOrTimeout(): AnswerReward = AnswerReward(0, 0, 0, 0, 0, 0)

    fun calculateLevel(totalXp: Long): Int = if (totalXp <= 0L) 1 else (totalXp / XP_PER_LEVEL).toInt() + 1

    fun calculateXpProgressInCurrentLevel(totalXp: Long): Float =
        if (totalXp <= 0L) 0f else (totalXp % XP_PER_LEVEL).toFloat() / XP_PER_LEVEL.toFloat()

    fun calculateXpToNextLevel(totalXp: Long): Long = XP_PER_LEVEL - (totalXp.coerceAtLeast(0L) % XP_PER_LEVEL)
}
