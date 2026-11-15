package com.brainrush.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brainrush.app.data.repository.BrainRushRepository
import com.brainrush.app.domain.engine.GameEngine
import com.brainrush.app.domain.engine.GameEngineState
import com.brainrush.app.domain.models.*
import com.brainrush.app.domain.scoring.ScoringEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class BrainRushViewModel(
    private val repository: BrainRushRepository,
    val scoringEngine: ScoringEngine = ScoringEngine()
) : ViewModel() {
    private val gameEngine = GameEngine(scoringEngine = scoringEngine)
    val userProgress: StateFlow<UserProgress> = repository.userProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProgress())
    val appSettings: StateFlow<AppSettings> = repository.appSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _selectedConfig = MutableStateFlow(GameConfig())
    val selectedConfig: StateFlow<GameConfig> = _selectedConfig.asStateFlow()
    val gameState: StateFlow<GameEngineState> = gameEngine.state

    private val _lastCompletedSession = MutableStateFlow<GameSessionRecord?>(null)
    val lastCompletedSession: StateFlow<GameSessionRecord?> = _lastCompletedSession.asStateFlow()

    private var timerLoopJob: Job? = null
    private var sessionPersisted = false

    fun selectMode(mode: GameMode) = _selectedConfig.update { it.copy(mode = mode) }
    fun setTrainingQuestionCount(count: Int) = _selectedConfig.update { it.copy(trainingQuestionCount = count.coerceIn(5, 50)) }
    fun selectDifficulty(difficulty: Difficulty) = _selectedConfig.update { it.copy(difficulty = difficulty) }

    fun toggleOperation(operation: MathOperation) {
        _selectedConfig.update { current ->
            if (operation == MathOperation.MIXED) return@update current.copy(operations = setOf(MathOperation.MIXED))
            val mutable = current.operations.toMutableSet().apply { remove(MathOperation.MIXED) }
            if (mutable.contains(operation)) { if (mutable.size > 1) mutable.remove(operation) } else mutable.add(operation)
            current.copy(operations = mutable)
        }
    }

    fun startQuickMode(mode: GameMode) {
        _selectedConfig.update { it.copy(mode = mode) }
        startGameSession()
    }

    fun startGameSession() {
        sessionPersisted = false
        _lastCompletedSession.value = null
        gameEngine.startNewGame(_selectedConfig.value)
        timerLoopJob?.cancel()
        timerLoopJob = viewModelScope.launch {
            while (isActive) {
                delay(50L)
                gameEngine.onTick()
                if (gameEngine.state.value.isGameOver) { handleGameFinished(); break }
            }
        }
    }

    fun onDigitPressed(digit: Char) = gameEngine.onDigitInput(digit)
    fun onDeletePressed() = gameEngine.onDeleteDigit()
    fun onSubmitPressed() {
        gameEngine.submitAnswer()
        if (gameEngine.state.value.isGameOver) handleGameFinished()
    }
    fun pauseGame() = gameEngine.pauseGame()
    fun resumeGame() = gameEngine.resumeGame()

    private fun handleGameFinished() {
        if (sessionPersisted) return
        sessionPersisted = true
        timerLoopJob?.cancel()
        viewModelScope.launch {
            val raw = gameEngine.buildSessionRecord(System.currentTimeMillis(), false)
            _lastCompletedSession.value = repository.recordCompletedSession(raw, gameEngine.getSessionOperationStats(), gameEngine.getTotalResponseTimeMillis())
        }
    }

    fun toggleSound(enabled: Boolean) { viewModelScope.launch { repository.updateSettings { it.copy(soundEnabled = enabled) } } }
    fun toggleVibration(enabled: Boolean) { viewModelScope.launch { repository.updateSettings { it.copy(vibrationEnabled = enabled) } } }
    fun setThemeMode(themeMode: AppThemeMode) { viewModelScope.launch { repository.updateSettings { it.copy(themeMode = themeMode) } } }
    fun resetAllStatistics() { viewModelScope.launch { repository.resetStatistics() } }

    class Factory(private val repository: BrainRushRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BrainRushViewModel(repository) as T
    }
}
