package com.brainrush.app.domain.engine

import android.os.SystemClock

fun interface TimeProvider { fun elapsedRealtime(): Long }
class SystemTimeProvider : TimeProvider { override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime() }

class TimerManager(private val timeProvider: TimeProvider = SystemTimeProvider()) {
    private var durationMs: Long = 0L
    private var startTimestampMs: Long = 0L
    private var accumulatedPausedMs: Long = 0L
    private var pauseStartedTimestampMs: Long? = null
    private var isRunning: Boolean = false

    fun start(durationMillis: Long) {
        durationMs = durationMillis.coerceAtLeast(0L)
        startTimestampMs = timeProvider.elapsedRealtime()
        accumulatedPausedMs = 0L
        pauseStartedTimestampMs = null
        isRunning = true
    }

    fun pause() {
        if (!isRunning || pauseStartedTimestampMs != null) return
        pauseStartedTimestampMs = timeProvider.elapsedRealtime()
    }

    fun resume() {
        val pausedAt = pauseStartedTimestampMs ?: return
        accumulatedPausedMs += (timeProvider.elapsedRealtime() - pausedAt).coerceAtLeast(0L)
        pauseStartedTimestampMs = null
    }

    fun stop() {
        isRunning = false
        pauseStartedTimestampMs = null
    }

    fun getElapsedMillis(): Long {
        if (!isRunning) return 0L
        val effectiveNow = pauseStartedTimestampMs ?: timeProvider.elapsedRealtime()
        return (effectiveNow - startTimestampMs - accumulatedPausedMs).coerceAtLeast(0L)
    }

    fun getRemainingMillis(): Long = if (!isRunning) 0L else (durationMs - getElapsedMillis()).coerceAtLeast(0L)
    fun getRemainingFraction(): Float = if (durationMs <= 0L) 0f else (getRemainingMillis().toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    fun isExpired(): Boolean = isRunning && getRemainingMillis() <= 0L
}
