package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.pdf.DateRange
import com.zenia.app.pdf.PdfExportConfig
import com.zenia.app.ui.components.ZeniaTopBar
import java.time.LocalDate

@Composable
fun ExportSettingsScreen(
    showTutorial: Boolean,
    isPremium: Boolean,
    onTutorialDismiss: () -> Unit,
    onGeneratePdf: (PdfExportConfig) -> Unit,
    onNavigateBack: () -> Unit
) {

    var selectedRangeType by remember { mutableStateOf("month") }

    var includeMood by remember { mutableStateOf(true) }
    var includeActivities by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var includeSmartwatch by remember { mutableStateOf(false) }
    var includeLogo by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Cápsula del Tiempo",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (showTutorial) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = "¿Qué es la Cápsula del Tiempo?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Aquí puedes generar un PDF con tus registros emocionales. Tú decides qué compartir y en qué periodo.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onTutorialDismiss) {
                            Text("Entendido")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---- PERIODO ----

            Text(
                text = "Periodo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExportRangeOption(
                title = "Hoy",
                selected = selectedRangeType == "day",
                onClick = { selectedRangeType = "day" }
            )

            ExportRangeOption(
                title = "Última semana",
                selected = selectedRangeType == "week",
                onClick = { selectedRangeType = "week" }
            )

            ExportRangeOption(
                title = "Último mes",
                selected = selectedRangeType == "month",
                onClick = { selectedRangeType = "month" }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---- CONTENIDO ----

            Text(
                text = "Incluir en el PDF",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            CheckboxRow("Estado de ánimo", includeMood) { includeMood = it }
            CheckboxRow("Actividades", includeActivities) { includeActivities = it }
            CheckboxRow("Notas", includeNotes) { includeNotes = it }

            if (isPremium) {
                CheckboxRow("Datos del smartwatch", includeSmartwatch) {
                    includeSmartwatch = it
                }
                CheckboxRow("Mostrar logo ZenIA", includeLogo) {
                    includeLogo = it
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    val today = LocalDate.now()

                    val range = when (selectedRangeType) {
                        "day" -> DateRange.SingleDay(today)
                        "week" -> DateRange.Period(today.minusWeeks(1), today)
                        else -> DateRange.Period(today.minusMonths(1), today)
                    }

                    val config = PdfExportConfig(
                        includeMood = includeMood,
                        includeActivities = includeActivities,
                        includeNotes = includeNotes,
                        includeSmartwatchData = includeSmartwatch,
                        includeLogo = includeLogo,
                        dateRange = range
                    )

                    onGeneratePdf(config)
                }
            ) {
                Text("Generar PDF")
            }
        }
    }
}

@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun ExportRangeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}