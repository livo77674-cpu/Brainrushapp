package com.brainrush.app.data.repository

import com.brainrush.app.data.preferences.BrainRushDataStore
import com.brainrush.app.data.statistics.StatisticsManager
import com.brainrush.app.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface BrainRushRepository {
    val userProgress: Flow<UserProgress>
    val appSettings: Flow<AppSettings>
    suspend fun recordCompletedSession(session: GameSessionRecord, operationStats: Map<MathOperation, OperationStat>, totalResponseTimeMs: Long): GameSessionRecord
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
    suspend fun resetStatistics()
}

class OfflineBrainRushRepository(
    private val dataStore: BrainRushDataStore,
    private val statisticsManager: StatisticsManager = StatisticsManager()
) : BrainRushRepository {
    override val userProgress: Flow<UserProgress> = dataStore.userProgressFlow
    override val appSettings: Flow<AppSettings> = dataStore.appSettingsFlow

    override suspend fun recordCompletedSession(
        session: GameSessionRecord,
        operationStats: Map<MathOperation, OperationStat>,
        totalResponseTimeMs: Long
    ): GameSessionRecord {
        val current = userProgress.first()
        val isNewRecord = session.score > 0 && session.score > current.bestScoreOverall
        val finalizedSession = session.copy(isNewRecord = isNewRecord)
        dataStore.saveUserProgress(statisticsManager.applySessionResult(current, finalizedSession, operationStats, totalResponseTimeMs))
        return finalizedSession
    }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dataStore.saveAppSettings(transform(appSettings.first()))
    }

    override suspend fun resetStatistics() = dataStore.resetAllProgress()
}
