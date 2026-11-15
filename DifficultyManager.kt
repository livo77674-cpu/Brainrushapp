package com.brainrush.app.domain.engine

import com.brainrush.app.domain.models.Difficulty
import com.brainrush.app.domain.models.GameMode

class DifficultyManager {
    fun resolveDifficulty(baseDifficulty: Difficulty, mode: GameMode, correctAnswersCount: Int): Difficulty {
        if (mode != GameMode.ENDLESS) return baseDifficulty
        val stepsUp = correctAnswersCount / 5
        val targetOrdinal = (baseDifficulty.ordinal + stepsUp).coerceAtMost(Difficulty.entries.lastIndex)
        return Difficulty.entries[targetOrdinal]
    }

    fun getQuestionTimeLimitMillis(difficulty: Difficulty): Long = difficulty.timePerQuestionSec * 1000L
    fun getSprintTotalTimeMillis(): Long = 60_000L
}
