package com.brainrush.app.presentation.statistics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.MathOperation
import com.brainrush.app.domain.models.UserProgress
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun StatisticsScreen(progress: UserProgress, onBack: () -> Unit) {
    val recentAccuracies = progress.recentSessions.take(15).reversed().map { it.accuracyPercent }
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Статистика", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BrainRushCard(modifier = Modifier.weight(1f)) { Text("ТРЕНИРОВОК", style = MaterialTheme.typography.labelSmall); Text("${progress.totalGamesPlayed}", style = MaterialTheme.typography.headlineMedium) }
                BrainRushCard(modifier = Modifier.weight(1f)) { Text("ТОЧНОСТЬ", style = MaterialTheme.typography.labelSmall); Text("${progress.overallAccuracyPercent.toInt()}%", style = MaterialTheme.typography.headlineMedium.copy(color = BrainRushColors.OrangeAccent)) }
            }
            BrainRushCard {
                Text("ДИНАМИКА ТОЧНОСТИ (%)", style = MaterialTheme.typography.labelSmall)
                AccuracyTrendChart(values = recentAccuracies)
            }
            BrainRushCard {
                Text("ПО ОПЕРАЦИЯМ", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))
                MathOperation.concreteOperations().forEach { op ->
                    val stat = progress.operationStats[op]
                    Text("${op.symbol} ${op.title}: ${stat?.solved ?: 0} шт. (${(stat?.accuracyPercent ?: 0f).toInt()}%)", style = MaterialTheme.typography.bodyLarge)
                }
            }
            progress.recentSessions.forEach { item ->
                BrainRushCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.mode.title} • ${item.difficulty.title}", style = MaterialTheme.typography.titleMedium)
                        Text("${item.score} очк.", style = MaterialTheme.typography.titleMedium.copy(color = BrainRushColors.OrangeAccent))
                    }
                }
            }
        }
    }
}
