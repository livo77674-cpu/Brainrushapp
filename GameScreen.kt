package com.brainrush.app.presentation.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.brainrush.app.domain.engine.*
import com.brainrush.app.domain.models.AppSettings
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors
import java.util.Locale

@Composable
fun GameScreen(
    state: GameEngineState,
    settings: AppSettings,
    onDigitInput: (Char) -> Unit,
    onDeleteDigit: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onQuitToHome: () -> Unit,
    onGameFinished: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val feedbackHelper = remember { FeedbackHelper(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) onPause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.isGameOver) { if (state.isGameOver) onGameFinished() }

    LaunchedEffect(state.feedback, state.questionIndex, state.wrongAnswers, state.skippedAnswers) {
        when (state.feedback) {
            AnswerFeedback.CORRECT -> feedbackHelper.playCorrect(settings.soundEnabled, settings.vibrationEnabled)
            AnswerFeedback.WRONG, AnswerFeedback.TIMEOUT -> feedbackHelper.playWrong(settings.soundEnabled, settings.vibrationEnabled)
            AnswerFeedback.NONE -> Unit
        }
    }

    val cardBorderColor by animateColorAsState(
        targetValue = when (state.feedback) {
            AnswerFeedback.CORRECT -> BrainRushColors.SuccessGreen
            AnswerFeedback.WRONG, AnswerFeedback.TIMEOUT -> BrainRushColors.ErrorRed
            AnswerFeedback.NONE -> BrainRushColors.SubtleBorder
        },
        label = "border"
    )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("${state.config.mode.title.uppercase()} • ${state.activeDifficulty.title.uppercase()}", style = MaterialTheme.typography.labelSmall.copy(color = BrainRushColors.OrangeAccent))
                    Text(state.currentQuestion?.operation?.title ?: "", style = MaterialTheme.typography.titleMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (state.totalQuestionsTarget != null) "${state.questionIndex} / ${state.totalQuestionsTarget}" else "№ ${state.questionIndex}", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onPause, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BrainRushColors.DarkGraphite)) {
                        Icon(Icons.Rounded.Pause, contentDescription = "Пауза", tint = BrainRushColors.WhiteText)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Очки: ${state.score}", style = MaterialTheme.typography.labelLarge)
                if (state.combo > 1) Text("Комбо x${state.combo}", style = MaterialTheme.typography.labelLarge.copy(color = BrainRushColors.OrangeAccent))
                if (state.maxLives != null && state.livesRemaining != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(state.maxLives) { idx ->
                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(if (idx < state.livesRemaining) BrainRushColors.OrangeAccent else BrainRushColors.ErrorRed.copy(alpha = 0.25f)))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ТАЙМЕР", style = MaterialTheme.typography.labelSmall)
                Text(String.format(Locale.US, "%.1f с", state.remainingTimeMs / 1000f), style = MaterialTheme.typography.headlineMedium.copy(color = BrainRushColors.OrangeAccent))
            }
            LinearProgressIndicator(progress = { state.remainingFraction }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = BrainRushColors.OrangeAccent, trackColor = BrainRushColors.DarkGraphite)
        }

        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = BrainRushColors.DarkGraphite, border = BorderStroke(2.dp, cardBorderColor)) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.lastRewardPopup ?: " ", style = MaterialTheme.typography.labelLarge.copy(color = BrainRushColors.OrangeAccent))
                Text(state.currentQuestion?.formattedExpression ?: "", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                Surface(modifier = Modifier.fillMaxWidth(0.72f).height(60.dp), shape = RoundedCornerShape(14.dp), color = BrainRushColors.PitchBlack, border = BorderStroke(1.5.dp, BrainRushColors.OrangeAccent)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(state.userInput.ifEmpty { "?" }, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (state.userInput.isEmpty()) BrainRushColors.SecondaryText else BrainRushColors.WhiteText)
                    }
                }
            }
        }

        CustomNumericKeypad(onDigitClick = onDigitInput, onDeleteClick = onDeleteDigit, onSubmitClick = onSubmitAnswer, submitEnabled = state.userInput.isNotEmpty())
    }

    if (state.isPaused && !state.isGameOver) {
        AlertDialog(
            onDismissRequest = onResume,
            containerColor = BrainRushColors.DarkGraphite,
            title = { Text("Пауза") },
            text = { Text("Таймер остановлен.") },
            confirmButton = { TextButton(onClick = onResume) { Text("ПРОДОЛЖИТЬ", color = BrainRushColors.OrangeAccent) } },
            dismissButton = { TextButton(onClick = onQuitToHome) { Text("ВЫЙТИ", color = BrainRushColors.SecondaryText) } }
        )
    }
}
