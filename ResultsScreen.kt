package com.brainrush.app.presentation.results

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainrush.app.domain.models.GameSessionRecord
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors
import java.util.Locale

@Composable
fun ResultsScreen(session: GameSessionRecord?, onPlayAgain: () -> Unit, onBackToHome: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("РЕЗУЛЬТАТЫ", style = MaterialTheme.typography.headlineLarge)
        if (session == null) return
        BrainRushCard(highlighted = true) {
            if (session.isNewRecord) Text("НОВЫЙ ЛИЧНЫЙ РЕКОРД!", style = MaterialTheme.typography.labelLarge.copy(color = BrainRushColors.OrangeAccent))
            Text("${session.score}", style = MaterialTheme.typography.displayLarge.copy(color = BrainRushColors.OrangeAccent))
            Text("Начислено: +${session.earnedXp} XP", style = MaterialTheme.typography.titleMedium.copy(color = BrainRushColors.SuccessGreen))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BrainRushCard(modifier = Modifier.weight(1f)) { Text("ВЕРНО", style = MaterialTheme.typography.labelSmall); Text("${session.correctAnswers}", style = MaterialTheme.typography.headlineMedium) }
            BrainRushCard(modifier = Modifier.weight(1f)) { Text("ОШИБКИ", style = MaterialTheme.typography.labelSmall); Text("${session.wrongAnswers}", style = MaterialTheme.typography.headlineMedium) }
            BrainRushCard(modifier = Modifier.weight(1f)) { Text("ТОЧНОСТЬ", style = MaterialTheme.typography.labelSmall); Text("${session.accuracyPercent.toInt()}%", style = MaterialTheme.typography.headlineMedium) }
        }
        BrainRushCard {
            Text("Среднее время: ${String.format(Locale.US, "%.2f с", session.averageResponseTimeSec)} • Лучшее комбо: x${session.bestCombo}", style = MaterialTheme.typography.titleMedium)
        }
        PrimaryActionButton(text = "ИГРАТЬ СНОВА", onClick = onPlayAgain)
        OutlinedButton(onClick = onBackToHome, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
            Text("НА ГЛАВНУЮ", color = BrainRushColors.WhiteText)
        }
    }
}
