package com.brainrush.app

import com.brainrush.app.data.statistics.StatisticsManager
import com.brainrush.app.domain.engine.*
import com.brainrush.app.domain.generator.QuestionGenerator
import com.brainrush.app.domain.models.*
import com.brainrush.app.domain.scoring.ScoringEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class BrainRushCoreTests {
    private class FakeClock(var now: Long = 1_000L) : TimeProvider { override fun elapsedRealtime(): Long = now }

    @Test
    fun testDivisionAndOperations() {
        val gen = QuestionGenerator(Random(42))
        Difficulty.entries.forEach { diff ->
            repeat(100) {
                val div = gen.generate(setOf(MathOperation.DIVISION), diff)
                assertNotEquals(0, div.rightOperand)
                assertEquals(0, div.leftOperand % div.rightOperand)
                assertEquals(div.leftOperand / div.rightOperand, div.correctAnswer)
            }
        }
    }

    @Test
    fun testScoringAndCombos() {
        val scoring = ScoringEngine()
        val fast = scoring.evaluateCorrectAnswer(4, 1_500L)
        assertEquals(250, fast.totalPointsEarned)
        assertEquals(5, fast.newCombo)
        assertEquals(0, scoring.evaluateWrongOrTimeout().newCombo)
    }

    @Test
    fun testTimerPauseAndResume() {
        val clock = FakeClock(1_000L)
        val timer = TimerManager(clock)
        timer.start(10_000L)
        clock.now += 2_000L
        timer.pause()
        clock.now += 50_000L
        timer.resume()
        assertEquals(8_000L, timer.getRemainingMillis())
    }

    @Test
    fun testSurvivalAndHistoryCap() {
        val clock = FakeClock()
        val engine = GameEngine(QuestionGenerator(Random(7)), timeProvider = clock)
        engine.startNewGame(GameConfig(GameMode.SURVIVAL, setOf(MathOperation.ADDITION), Difficulty.EASY))
        repeat(3) {
            val wrong = engine.state.value.currentQuestion!!.correctAnswer + 99
            wrong.toString().forEach { engine.onDigitInput(it) }
            engine.submitAnswer()
        }
        assertTrue(engine.state.value.isGameOver)

        val stats = StatisticsManager()
        var progress = UserProgress()
        repeat(55) { i ->
            progress = stats.applySessionResult(
                progress,
                GameSessionRecord(i.toLong(), i.toLong(), GameMode.TRAINING, Difficulty.EASY, listOf(MathOperation.ADDITION), 100, 1, 0, 0, 100f, 1f, 1, 100, false),
                emptyMap(),
                1_000L
            )
        }
        assertEquals(50, progress.recentSessions.size)
    }
}
