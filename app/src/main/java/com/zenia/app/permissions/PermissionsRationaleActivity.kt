package com.zenia.app.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.theme.ZenIATheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenIATheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ZenIA requiere acceso a tus datos de salud",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Para poder sincronizar y mostrarte métricas precisas de tu salud.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Permisos solicitados:",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "• Pasos: para contar tus actividades diarias",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Sueño: para monitorear tu calidad de descanso",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Ritmo cardíaco: para análisis de salud cardiovascular",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}