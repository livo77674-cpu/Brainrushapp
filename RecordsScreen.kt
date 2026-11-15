package com.brainrush.app.presentation.records

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.Difficulty
import com.brainrush.app.domain.models.GameMode
import com.brainrush.app.domain.models.UserProgress
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun RecordsScreen(progress: UserProgress, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Рекорды", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BrainRushCard(highlighted = true) {
                Text("ЛУЧШИЙ РЕЗУЛЬТАТ", style = MaterialTheme.typography.labelSmall)
                Text("${progress.bestScoreOverall} ОЧКОВ", style = MaterialTheme.typography.displayMedium.copy(color = BrainRushColors.OrangeAccent))
            }
            BrainRushCard {
                Text("ПО РЕЖИМАМ", style = MaterialTheme.typography.labelSmall)
                GameMode.entries.forEach { mode ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(mode.title)
                        Text("${progress.bestScoreByMode[mode] ?: 0} очк.", color = BrainRushColors.OrangeAccent)
                    }
                }
            }
            BrainRushCard {
                Text("ПО СЛОЖНОСТИ", style = MaterialTheme.typography.labelSmall)
                Difficulty.entries.forEach { diff ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(diff.title)
                        Text("${progress.bestScoreByDifficulty[diff] ?: 0} очк.", color = BrainRushColors.OrangeAccent)
                    }
                }
            }
        }
    }
}
