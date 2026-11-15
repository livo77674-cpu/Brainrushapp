package com.brainrush.app.domain.engine

import com.brainrush.app.domain.generator.QuestionGenerator
import com.brainrush.app.domain.models.*
import com.brainrush.app.domain.scoring.ScoringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AnswerFeedback { NONE, CORRECT, WRONG, TIMEOUT }

data class GameEngineState(
    val config: GameConfig = GameConfig(),
    val activeDifficulty: Difficulty = Difficulty.EASY,
    val currentQuestion: MathQuestion? = null,
    val userInput: String = "",
    val questionIndex: Int = 1,
    val totalQuestionsTarget: Int? = null,
    val score: Int = 0,
    val earnedXp: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val skippedAnswers: Int = 0,
    val livesRemaining: Int? = null,
    val maxLives: Int? = null,
    val remainingTimeMs: Long = 0L,
    val totalTimeLimitMs: Long = 10_000L,
    val remainingFraction: Float = 1f,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val feedback: AnswerFeedback = AnswerFeedback.NONE,
    val lastRewardPopup: String? = null
)

class GameEngine(
    private val questionGenerator: QuestionGenerator = QuestionGenerator(),
    private val scoringEngine: ScoringEngine = ScoringEngine(),
    private val difficultyManager: DifficultyManager = DifficultyManager(),
    timeProvider: TimeProvider = SystemTimeProvider()
) {
    private val questionTimer = TimerManager(timeProvider)
    private val sprintTimer = TimerManager(timeProvider)
    private val _state = MutableStateFlow(GameEngineState())
    val state: StateFlow<GameEngineState> = _state.asStateFlow()

    private var activeQuestionIdHandled: Long = -1L
    private var totalResponseTimeMs: Long = 0L
    private val perOperationStats = mutableMapOf<MathOperation, OperationStat>()

    fun startNewGame(config: GameConfig) {
        questionGenerator.resetHistory()
        activeQuestionIdHandled = -1L
        totalResponseTimeMs = 0L
        perOperationStats.clear()

        val initialDifficulty = difficultyManager.resolveDifficulty(config.difficulty, config.mode, 0)
        val firstQuestion = questionGenerator.generate(config.operations, initialDifficulty)
        val maxLives = when (config.mode) {
            GameMode.SURVIVAL -> 3
            GameMode.ENDLESS -> 1
            else -> null
        }
        val targetQuestions = if (config.mode == GameMode.TRAINING) config.trainingQuestionCount else null
        val timeLimitMs = if (config.mode == GameMode.SPRINT) {
            sprintTimer.start(difficultyManager.getSprintTotalTimeMillis())
            questionTimer.start(difficultyManager.getSprintTotalTimeMillis())
            difficultyManager.getSprintTotalTimeMillis()
        } else {
            val qLimit = difficultyManager.getQuestionTimeLimitMillis(initialDifficulty)
            questionTimer.start(qLimit)
            qLimit
        }

        _state.value = GameEngineState(
            config = config,
            activeDifficulty = initialDifficulty,
            currentQuestion = firstQuestion,
            totalQuestionsTarget = targetQuestions,
            livesRemaining = maxLives,
            maxLives = maxLives,
            remainingTimeMs = timeLimitMs,
            totalTimeLimitMs = timeLimitMs
        )
    }

    fun onDigitInput(digit: Char) {
        val current = _state.value
        if (current.isGameOver || current.isPaused || !digit.isDigit() || current.userInput.length >= 7) return
        val nextInput = if (current.userInput == "0") digit.toString() else current.userInput + digit
        _state.update { it.copy(userInput = nextInput, feedback = AnswerFeedback.NONE) }
    }

    fun onDeleteDigit() {
        val current = _state.value
        if (current.isGameOver || current.isPaused || current.userInput.isEmpty()) return
        _state.update { it.copy(userInput = it.userInput.dropLast(1), feedback = AnswerFeedback.NONE) }
    }

    fun submitAnswer() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        val question = current.currentQuestion ?: return
        if (current.userInput.isBlank() || activeQuestionIdHandled == question.id) return
        activeQuestionIdHandled = question.id

        val parsedAnswer = current.userInput.toIntOrNull()
        val responseTimeMs = questionTimer.getElapsedMillis()
        totalResponseTimeMs += responseTimeMs

        val isCorrect = parsedAnswer != null && parsedAnswer == question.correctAnswer
        recordOperationAttempt(question.operation, isCorrect, responseTimeMs)

        if (isCorrect) {
            val reward = scoringEngine.evaluateCorrectAnswer(current.combo, responseTimeMs)
            val popup = buildString {
                append("+${reward.totalPointsEarned}")
                if (reward.speedBonus > 0) append(" • БЫСТРО!")
                if (reward.comboBonus > 0) append(" • КОМБО x${reward.newCombo}!")
            }
            _state.update {
                it.copy(
                    score = it.score + reward.totalPointsEarned,
                    earnedXp = it.earnedXp + reward.xpEarned,
                    combo = reward.newCombo,
                    bestCombo = maxOf(it.bestCombo, reward.newCombo),
                    correctAnswers = it.correctAnswers + 1,
                    feedback = AnswerFeedback.CORRECT,
                    lastRewardPopup = popup
                )
            }
            advanceOrFinish()
        } else {
            scoringEngine.evaluateWrongOrTimeout()
            val updatedLives = current.livesRemaining?.let { (it - 1).coerceAtLeast(0) }
            _state.update {
                it.copy(
                    combo = 0,
                    wrongAnswers = it.wrongAnswers + 1,
                    livesRemaining = updatedLives,
                    feedback = AnswerFeedback.WRONG,
                    lastRewardPopup = "Ошибка! Ответ: ${question.correctAnswer}"
                )
            }
            advanceOrFinish()
        }
    }

    fun onTick() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        val question = current.currentQuestion ?: return

        if (current.config.mode == GameMode.SPRINT) {
            _state.update { it.copy(remainingTimeMs = sprintTimer.getRemainingMillis(), remainingFraction = sprintTimer.getRemainingFraction()) }
            if (sprintTimer.isExpired()) finishGame()
        } else {
            _state.update { it.copy(remainingTimeMs = questionTimer.getRemainingMillis(), remainingFraction = questionTimer.getRemainingFraction()) }
            if (questionTimer.isExpired() && activeQuestionIdHandled != question.id) {
                activeQuestionIdHandled = question.id
                val elapsed = questionTimer.getElapsedMillis()
                totalResponseTimeMs += elapsed
                recordOperationAttempt(question.operation, false, elapsed)
                val updatedLives = current.livesRemaining?.let { (it - 1).coerceAtLeast(0) }
                _state.update {
                    it.copy(
                        combo = 0,
                        skippedAnswers = it.skippedAnswers + 1,
                        livesRemaining = updatedLives,
                        feedback = AnswerFeedback.TIMEOUT,
                        lastRewardPopup = "Время вышло!"
                    )
                }
                advanceOrFinish()
            }
        }
    }

    fun pauseGame() {
        val current = _state.value
        if (current.isGameOver || current.isPaused) return
        questionTimer.pause()
        if (current.config.mode == GameMode.SPRINT) sprintTimer.pause()
        _state.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        val current = _state.value
        if (current.isGameOver || !current.isPaused) return
        questionTimer.resume()
        if (current.config.mode == GameMode.SPRINT) sprintTimer.resume()
        _state.update { it.copy(isPaused = false) }
    }

    private fun advanceOrFinish() {
        val current = _state.value
        if ((current.config.mode == GameMode.SPRINT && sprintTimer.isExpired()) ||
            (current.livesRemaining != null && current.livesRemaining <= 0) ||
            (current.totalQuestionsTarget != null && current.questionIndex >= current.totalQuestionsTarget)
        ) {
            finishGame()
            return
        }

        val nextDifficulty = difficultyManager.resolveDifficulty(current.config.difficulty, current.config.mode, current.correctAnswers)
        val nextQuestion = questionGenerator.generate(current.config.operations, nextDifficulty)
        val nextLimitMs = if (current.config.mode == GameMode.SPRINT) {
            questionTimer.start(sprintTimer.getRemainingMillis())
            difficultyManager.getSprintTotalTimeMillis()
        } else {
            difficultyManager.getQuestionTimeLimitMillis(nextDifficulty).also { questionTimer.start(it) }
        }
        val remainingMs = if (current.config.mode == GameMode.SPRINT) sprintTimer.getRemainingMillis() else nextLimitMs

        _state.update {
            it.copy(
                activeDifficulty = nextDifficulty,
                currentQuestion = nextQuestion,
                userInput = "",
                questionIndex = it.questionIndex + 1,
                remainingTimeMs = remainingMs,
                totalTimeLimitMs = nextLimitMs,
                remainingFraction = if (current.config.mode == GameMode.SPRINT) sprintTimer.getRemainingFraction() else 1f
            )
        }
    }

    private fun finishGame() {
        questionTimer.stop()
        sprintTimer.stop()
        _state.update { it.copy(isGameOver = true, isPaused = false, remainingTimeMs = 0L, remainingFraction = 0f) }
    }

    private fun recordOperationAttempt(operation: MathOperation, isCorrect: Boolean, responseTimeMs: Long) {
        val prev = perOperationStats[operation] ?: OperationStat()
        perOperationStats[operation] = OperationStat(
            solved = prev.solved + 1,
            correct = prev.correct + if (isCorrect) 1 else 0,
            totalResponseTimeMs = prev.totalResponseTimeMs + responseTimeMs
        )
    }

    fun getSessionOperationStats(): Map<MathOperation, OperationStat> = perOperationStats.toMap()
    fun getTotalResponseTimeMillis(): Long = totalResponseTimeMs

    fun buildSessionRecord(timestampMillis: Long, isNewRecord: Boolean): GameSessionRecord {
        val st = _state.value
        val totalAttempts = (st.correctAnswers + st.wrongAnswers + st.skippedAnswers).coerceAtLeast(1)
        return GameSessionRecord(
            id = timestampMillis,
            timestampMillis = timestampMillis,
            mode = st.config.mode,
            difficulty = st.config.difficulty,
            operations = st.config.operations.toList(),
            score = st.score,
            correctAnswers = st.correctAnswers,
            wrongAnswers = st.wrongAnswers,
            skippedAnswers = st.skippedAnswers,
            accuracyPercent = (st.correctAnswers.toFloat() / totalAttempts.toFloat()) * 100f,
            averageResponseTimeSec = (totalResponseTimeMs / 1000f) / totalAttempts.toFloat(),
            bestCombo = st.bestCombo,
            earnedXp = st.earnedXp,
            isNewRecord = isNewRecord
        )
    }
}
