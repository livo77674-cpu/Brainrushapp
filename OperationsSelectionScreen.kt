package com.brainrush.app.presentation.operations

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainrush.app.domain.models.MathOperation
import com.brainrush.app.presentation.components.*
import com.brainrush.app.ui.colors.BrainRushColors

@Composable
fun OperationsSelectionScreen(selectedOperations: Set<MathOperation>, onToggleOperation: (MathOperation) -> Unit, onContinue: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BrainRushTopBar(title = "Операции", onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MathOperation.entries.forEach { op ->
                val isSelected = selectedOperations.contains(op)
                BrainRushCard(highlighted = isSelected, onClick = { onToggleOperation(op) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(op.symbol, fontSize = 28.sp, fontWeight = FontWeight.Black, color = BrainRushColors.OrangeAccent)
                            Text(op.title, style = MaterialTheme.typography.titleLarge)
                        }
                        Icon(if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (isSelected) BrainRushColors.OrangeAccent else BrainRushColors.SecondaryText)
                    }
                }
            }
        }
        PrimaryActionButton(text = "ДАЛЕЕ: СЛОЖНОСТЬ", onClick = onContinue, modifier = Modifier.padding(20.dp))
    }
}
