package com.brainrush.app.presentation.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.BuildConfig
import com.brainrush.app.domain.models.AppSettings
import com.brainrush.app.domain.models.AppThemeMode
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
    onConfirmResetStatistics: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Настройки", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BrainRushCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Звуковые эффекты", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = settings.soundEnabled, onCheckedChange = onToggleSound)
                }
            }
            BrainRushCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = settings.vibrationEnabled, onCheckedChange = onToggleVibration)
                }
            }
            BrainRushCard {
                Text("Тема", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(selected = settings.themeMode == mode, onClick = { onSelectTheme(mode) }, label = { Text(mode.title) })
                    }
                }
            }
            BrainRushCard {
                Text("Brain Rush v${BuildConfig.VERSION_NAME} • 100% Offline", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("СБРОСИТЬ СТАТИСТИКУ", color = BrainRushColors.ErrorRed)
            }
        }
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить прогресс?") },
            text = { Text("Все очки, XP и рекорды будут удалены.") },
            confirmButton = { TextButton(onClick = { onConfirmResetStatistics(); showResetDialog = false }) { Text("УДАЛИТЬ", color = BrainRushColors.ErrorRed) } },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("ОТМЕНА") } }
        )
    }
}
