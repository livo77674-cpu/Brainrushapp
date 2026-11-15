package com.brainrush.app.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun BrainRushTopBar(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(BrainRushColors.DarkGraphite)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад", tint = BrainRushColors.WhiteText)
            }
            Spacer(modifier = Modifier.size(12.dp))
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun BrainRushCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = if (highlighted) BrainRushColors.DarkBrownAccent else BrainRushColors.DarkGraphite,
        border = BorderStroke(1.dp, if (highlighted) BrainRushColors.OrangeAccent else BrainRushColors.SubtleBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrainRushColors.OrangeAccent, contentColor = BrainRushColors.PitchBlack)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = BrainRushColors.PitchBlack))
    }
}

@Composable
fun CustomNumericKeypad(onDigitClick: (Char) -> Unit, onDeleteClick: () -> Unit, onSubmitClick: () -> Unit, submitEnabled: Boolean) {
    val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowDigits ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowDigits.forEach { digit ->
                    KeypadButton(label = digit.toString(), onClick = { onDigitClick(digit) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                modifier = Modifier.weight(1f).height(62.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onDeleteClick),
                shape = RoundedCornerShape(16.dp),
                color = BrainRushColors.DarkGraphite,
                border = BorderStroke(1.dp, BrainRushColors.SubtleBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Удалить", tint = BrainRushColors.WhiteText, modifier = Modifier.size(26.dp))
                }
            }
            KeypadButton(label = "0", onClick = { onDigitClick('0') }, modifier = Modifier.weight(1f))
            Surface(
                modifier = Modifier.weight(1f).height(62.dp).clip(RoundedCornerShape(16.dp)).clickable(enabled = submitEnabled, onClick = onSubmitClick),
                shape = RoundedCornerShape(16.dp),
                color = if (submitEnabled) BrainRushColors.OrangeAccent else BrainRushColors.ElevatedGraphite
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = "Ответить", tint = if (submitEnabled) BrainRushColors.PitchBlack else BrainRushColors.SecondaryText, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(62.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = BrainRushColors.DarkGraphite,
        border = BorderStroke(1.dp, BrainRushColors.SubtleBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = BrainRushColors.WhiteText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AccuracyTrendChart(values: List<Float>) {
    if (values.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
            Text("Сыграйте первую тренировку для построения графика", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp).padding(vertical = 8.dp)) {
        val stepX = if (values.size > 1) size.width / (values.size - 1) else size.width / 2f
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2f else index * stepX
            val y = size.height - (value.coerceIn(0f, 100f) / 100f) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (values.size > 1) drawPath(path = path, color = BrainRushColors.OrangeAccent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2f else index * stepX
            val y = size.height - (value.coerceIn(0f, 100f) / 100f) * size.height
            drawCircle(color = BrainRushColors.OrangeAccent, radius = 4.5.dp.toPx(), center = Offset(x, y))
        }
    }
}
