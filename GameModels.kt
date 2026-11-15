package com.brainrush.app.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(val title: String, val subtitle: String, val badge: String) {
    TRAINING("Тренировка", "Настройте операции, сложность и число примеров", "КЛАССИКА"),
    SPRINT("Спринт", "Максимум правильных ответов за 60 секунд", "60 СЕК"),
    SURVIVAL("Выживание", "Три ошибки — игра окончена", "3 ЖИЗНИ"),
    ENDLESS("Бесконечность", "Постепенный рост сложности до первой ошибки или тайм-аута", "ХАРДКОР")
}

@Serializable
enum class MathOperation(val title: String, val symbol: String) {
    ADDITION("Сложение", "+"),
    SUBTRACTION("Вычитание", "−"),
    MULTIPLICATION("Умножение", "×"),
    DIVISION("Деление", "÷"),
    MIXED("Смешанные", "±×÷");

    companion object {
        fun concreteOperations(): List<MathOperation> =
            listOf(ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION)
    }
}

@Serializable
enum class Difficulty(val title: String, val timePerQuestionSec: Int, val description: String) {
    EASY("Лёгкий", 10, "Числа до 20 • 10 сек на ответ"),
    MEDIUM("Средний", 8, "Двузначные числа • 8 сек на ответ"),
    HARD("Сложный", 6, "Трёхзначные числа • 6 сек на ответ"),
    EXPERT("Эксперт", 5, "Большие числа • 5 сек на ответ")
}

data class GameConfig(
    val mode: GameMode = GameMode.TRAINING,
    val operations: Set<MathOperation> = setOf(MathOperation.ADDITION, MathOperation.SUBTRACTION),
    val difficulty: Difficulty = Difficulty.EASY,
    val trainingQuestionCount: Int = 15
)

data class MathQuestion(
    val id: Long,
    val leftOperand: Int,
    val rightOperand: Int,
    val operation: MathOperation,
    val correctAnswer: Int
) {
    val formattedExpression: String get() = "$leftOperand ${operation.symbol} $rightOperand"
}

@Serializable
data class OperationStat(
    val solved: Int = 0,
    val correct: Int = 0,
    val totalResponseTimeMs: Long = 0L
) {
    val accuracyPercent: Float get() = if (solved == 0) 0f else (correct.toFloat() / solved.toFloat()) * 100f
    val averageTimeSec: Float get() = if (solved == 0) 0f else (totalResponseTimeMs / 1000f) / solved.toFloat()
}

@Serializable
data class GameSessionRecord(
    val id: Long,
    val timestampMillis: Long,
    val mode: GameMode,
    val difficulty: Difficulty,
    val operations: List<MathOperation>,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val skippedAnswers: Int,
    val accuracyPercent: Float,
    val averageResponseTimeSec: Float,
    val bestCombo: Int,
    val earnedXp: Int,
    val isNewRecord: Boolean
)

@Serializable
data class UserProgress(
    val totalXp: Long = 0L,
    val level: Int = 1,
    val totalGamesPlayed: Int = 0,
    val totalQuestionsSolved: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalWrongAnswers: Int = 0,
    val totalResponseTimeMs: Long = 0L,
    val bestScoreOverall: Int = 0,
    val bestAccuracyOverall: Float = 0f,
    val longestComboOverall: Int = 0,
    val bestScoreByMode: Map<GameMode, Int> = emptyMap(),
    val bestScoreByDifficulty: Map<Difficulty, Int> = emptyMap(),
    val operationStats: Map<MathOperation, OperationStat> = emptyMap(),
    val recentSessions: List<GameSessionRecord> = emptyList()
) {
    val overallAccuracyPercent: Float
        get() = if (totalQuestionsSolved == 0) 0f else (totalCorrectAnswers.toFloat() / totalQuestionsSolved.toFloat()) * 100f
    val overallAverageTimeSec: Float
        get() = if (totalQuestionsSolved == 0) 0f else (totalResponseTimeMs / 1000f) / totalQuestionsSolved.toFloat()
}

@Serializable
enum class AppThemeMode(val title: String) {
    OBSIDIAN("Тёмный обсидиан"),
    WARM_ESPRESSO("Тёплый графит")
}

@Serializable
data class AppSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.OBSIDIAN
)
