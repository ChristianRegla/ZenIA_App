package com.zenia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.util.AnalysisUtils

@Composable
fun MoodPatternsCard(
    topBooster: AnalysisUtils.Insight?,
    topDrainer: AnalysisUtils.Insight?
) {
    if (topBooster == null && topDrainer == null) return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Descubrimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (topBooster == null && topDrainer == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sigue registrando tus días. Pronto descubriré qué te hace sentir bien.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            else {
                if (topBooster != null) {
                    InsightRow(
                        label = "Te hace sentir increíble:",
                        activity = topBooster.activityName,
                        iconTint = Color(0xFF4CAF50),
                        isPositive = true
                    )
                }

                if (topBooster != null && topDrainer != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                if (topDrainer != null) {
                    InsightRow(
                        label = "Sueles sentirte mal con:",
                        activity = topDrainer.activityName,
                        iconTint = Color(0xFFEF5350),
                        isPositive = false
                    )
                }
            }
        }
    }
}

@Composable
fun InsightRow(label: String, activity: String, iconTint: Color, isPositive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(
                text = activity,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}