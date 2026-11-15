package com.brainrush.app.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.GameMode
import com.brainrush.app.domain.models.UserProgress
import com.brainrush.app.domain.scoring.ScoringEngine
import com.brainrush.app.presentation.components.BrainRushCard
import com.brainrush.app.presentation.components.PrimaryActionButton
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun HomeScreen(
    progress: UserProgress,
    scoringEngine: ScoringEngine,
    onStartFlow: () -> Unit,
    onQuickStartMode: (GameMode) -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val levelProgress = scoringEngine.calculateXpProgressInCurrentLevel(progress.totalXp)
    val xpToNext = scoringEngine.calculateXpToNextLevel(progress.totalXp)
    val lastSession = progress.recentSessions.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(BrainRushColors.OrangeAccent), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = BrainRushColors.PitchBlack, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text("BRAIN RUSH", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                    Text("Тренажёр скорости мышления", style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(BrainRushColors.DarkGraphite)) {
                Icon(Icons.Rounded.Settings, contentDescription = "Настройки", tint = BrainRushColors.WhiteText)
            }
        }

        BrainRushCard(highlighted = true) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("УРОВЕНЬ ${progress.level}", style = MaterialTheme.typography.labelLarge.copy(color = BrainRushColors.OrangeAccent))
                    Text("${progress.totalXp} XP", style = MaterialTheme.typography.headlineLarge)
                }
                Text("До след.: $xpToNext XP", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { levelProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = BrainRushColors.OrangeAccent, trackColor = BrainRushColors.PitchBlack)
        }

        PrimaryActionButton(text = "НАЧАТЬ ТРЕНИРОВКУ", onClick = onStartFlow)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BrainRushCard(modifier = Modifier.weight(1f)) {
                Text("ЛИЧНЫЙ РЕКОРД", style = MaterialTheme.typography.labelSmall)
                Text("${progress.bestScoreOverall}", style = MaterialTheme.typography.headlineMedium.copy(color = BrainRushColors.OrangeAccent))
            }
            BrainRushCard(modifier = Modifier.weight(1f)) {
                Text("ТРЕНИРОВОК", style = MaterialTheme.typography.labelSmall)
                Text("${progress.totalGamesPlayed}", style = MaterialTheme.typography.headlineMedium)
            }
        }

        BrainRushCard {
            Text("ПОСЛЕДНЯЯ ТРЕНИРОВКА", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (lastSession != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${lastSession.mode.title} • ${lastSession.difficulty.title}", style = MaterialTheme.typography.titleMedium)
                        Text("Точность: ${lastSession.accuracyPercent.toInt()}% • Комбо: x${lastSession.bestCombo}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${lastSession.score} очк.", style = MaterialTheme.typography.titleLarge.copy(color = BrainRushColors.OrangeAccent))
                }
            } else {
                Text("Ещё нет завершённых сессий. Запустите первую разминку!", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BrainRushCard(modifier = Modifier.weight(1f), onClick = { onQuickStartMode(GameMode.SPRINT) }) {
                Text("60 СЕК", style = MaterialTheme.typography.labelSmall.copy(color = BrainRushColors.OrangeAccent))
                Text("Спринт", style = MaterialTheme.typography.titleMedium)
            }
            BrainRushCard(modifier = Modifier.weight(1f), onClick = { onQuickStartMode(GameMode.SURVIVAL) }) {
                Text("3 ЖИЗНИ", style = MaterialTheme.typography.labelSmall.copy(color = BrainRushColors.OrangeAccent))
                Text("Выживание", style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BrainRushCard(modifier = Modifier.weight(1f), onClick = onOpenStatistics) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = BrainRushColors.OrangeAccent)
                Text("Статистика", style = MaterialTheme.typography.titleMedium)
            }
            BrainRushCard(modifier = Modifier.weight(1f), onClick = onOpenRecords) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = BrainRushColors.OrangeAccent)
                Text("Рекорды", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
