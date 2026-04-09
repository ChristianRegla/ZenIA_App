package com.zenia.app.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZenIATheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenIATheme {
                PermissionsRationaleScreen(onClose = { finish() })
            }
        }
    }
}

@Composable
fun PermissionsRationaleScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Permisos de Salud",
                onNavigateBack = onClose
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ZeniaTeal,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "¿Por qué necesitamos estos permisos?",
                        style = MaterialTheme.typography.titleMedium,
                        color = ZeniaSlateGrey
                    )
                    Text(
                        "Para brindarte la mejor experiencia",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeniaSlateGrey.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "ZenIA requiere acceso a tus datos de salud para poder sincronizar automáticamente tu información desde Health Connect. Esto nos permite ofrecerte análisis precisos y personalizados.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZeniaSlateGrey,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Permisos solicitados:",
                style = MaterialTheme.typography.titleMedium,
                color = ZeniaSlateGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                title = "Ritmo Cardíaco",
                description = "Monitoreamos tu ritmo cardíaco para detectar cambios en tu salud cardiovascular y ofrecerte recomendaciones personalizadas."
            )

            PermissionItem(
                title = "Pasos y Actividad",
                description = "Contamos tus pasos para calcular tu actividad física diaria y mostrarte progreso hacia tus objetivos."
            )

            PermissionItem(
                title = "Sueño",
                description = "Analizamos tus patrones de sueño para ayudarte a mejorar la calidad de tu descanso."
            )

            PermissionItem(
                title = "Variabilidad del Ritmo Cardíaco (HRV)",
                description = "Evaluamos tu nivel de estrés y recuperación basándonos en la variabilidad de tu ritmo cardíaco."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Privacy Note
            Text(
                "Tu privacidad es importante",
                style = MaterialTheme.typography.titleSmall,
                color = ZeniaSlateGrey
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• Tus datos se almacenan localmente en Health Connect\n• Puedes revocar permisos en cualquier momento\n• No compartimos tus datos sin consentimiento",
                style = MaterialTheme.typography.bodySmall,
                color = ZeniaSlateGrey.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZeniaTeal,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entendido", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ZeniaTeal,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = ZeniaSlateGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ZeniaSlateGrey.copy(alpha = 0.8f)
                )
            }
        }
    }
}