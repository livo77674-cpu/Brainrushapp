package com.brainrush.app.data.statistics

import com.brainrush.app.domain.models.*
import com.brainrush.app.domain.scoring.ScoringEngine

class StatisticsManager(private val scoringEngine: ScoringEngine = ScoringEngine()) {
    companion object { const val MAX_RECENT_SESSIONS = 50 }

    fun applySessionResult(
        currentProgress: UserProgress,
        session: GameSessionRecord,
        sessionOperationStats: Map<MathOperation, OperationStat>,
        sessionResponseTimeMs: Long
    ): UserProgress {
        val updatedTotalXp = currentProgress.totalXp + session.earnedXp
        val sessionQuestionsCount = session.correctAnswers + session.wrongAnswers + session.skippedAnswers
        val updatedModeRecords = currentProgress.bestScoreByMode.toMutableMap().apply {
            if (session.score > (this[session.mode] ?: 0)) this[session.mode] = session.score
        }
        val updatedDifficultyRecords = currentProgress.bestScoreByDifficulty.toMutableMap().apply {
            if (session.score > (this[session.difficulty] ?: 0)) this[session.difficulty] = session.score
        }
        val updatedOpStats = currentProgress.operationStats.toMutableMap().apply {
            for ((op, delta) in sessionOperationStats) {
                val existing = this[op] ?: OperationStat()
                this[op] = OperationStat(
                    solved = existing.solved + delta.solved,
                    correct = existing.correct + delta.correct,
                    totalResponseTimeMs = existing.totalResponseTimeMs + delta.totalResponseTimeMs
                )
            }
        }
        return currentProgress.copy(
            totalXp = updatedTotalXp,
            level = scoringEngine.calculateLevel(updatedTotalXp),
            totalGamesPlayed = currentProgress.totalGamesPlayed + 1,
            totalQuestionsSolved = currentProgress.totalQuestionsSolved + sessionQuestionsCount,
            totalCorrectAnswers = currentProgress.totalCorrectAnswers + session.correctAnswers,
            totalWrongAnswers = currentProgress.totalWrongAnswers + session.wrongAnswers + session.skippedAnswers,
            totalResponseTimeMs = currentProgress.totalResponseTimeMs + sessionResponseTimeMs,
            bestScoreOverall = maxOf(currentProgress.bestScoreOverall, session.score),
            bestAccuracyOverall = if (sessionQuestionsCount > 0) maxOf(currentProgress.bestAccuracyOverall, session.accuracyPercent) else currentProgress.bestAccuracyOverall,
            longestComboOverall = maxOf(currentProgress.longestComboOverall, session.bestCombo),
            bestScoreByMode = updatedModeRecords,
            bestScoreByDifficulty = updatedDifficultyRecords,
            operationStats = updatedOpStats,
            recentSessions = (listOf(session) + currentProgress.recentSessions).take(MAX_RECENT_SESSIONS)
        )
    }
}
