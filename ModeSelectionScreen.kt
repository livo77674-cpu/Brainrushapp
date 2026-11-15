package com.brainrush.app.presentation.modes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.GameConfig
import com.brainrush.app.domain.models.GameMode
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun ModeSelectionScreen(config: GameConfig, onSelectMode: (GameMode) -> Unit, onSelectQuestionCount: (Int) -> Unit, onContinue: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Выбор режима", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GameMode.entries.forEach { mode ->
                val isSelected = config.mode == mode
                BrainRushCard(highlighted = isSelected, onClick = { onSelectMode(mode) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(mode.title, style = MaterialTheme.typography.titleLarge)
                        Text(mode.badge, style = MaterialTheme.typography.labelSmall.copy(color = BrainRushColors.OrangeAccent))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(mode.subtitle, style = MaterialTheme.typography.bodyMedium)
                    if (mode == GameMode.TRAINING && isSelected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(10, 15, 25, 40).forEach { count ->
                                FilterChip(selected = config.trainingQuestionCount == count, onClick = { onSelectQuestionCount(count) }, label = { Text("$count") })
                            }
                        }
                    }
                }
            }
        }
        PrimaryActionButton(text = "ДАЛЕЕ: ОПЕРАЦИИ", onClick = onContinue, modifier = Modifier.padding(20.dp))
    }
}
