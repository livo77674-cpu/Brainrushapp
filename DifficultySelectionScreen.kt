package com.brainrush.app.presentation.difficulty

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.Difficulty
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun DifficultySelectionScreen(selectedDifficulty: Difficulty, onSelectDifficulty: (Difficulty) -> Unit, onStartGame: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Сложность", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Difficulty.entries.forEach { diff ->
                BrainRushCard(highlighted = selectedDifficulty == diff, onClick = { onSelectDifficulty(diff) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(diff.title, style = MaterialTheme.typography.titleLarge)
                        Text("${diff.timePerQuestionSec} СЕК", style = MaterialTheme.typography.labelLarge.copy(color = BrainRushColors.OrangeAccent))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(diff.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        PrimaryActionButton(text = "СТАРТ ИГРЫ", onClick = onStartGame, modifier = Modifier.padding(20.dp))
    }
}
