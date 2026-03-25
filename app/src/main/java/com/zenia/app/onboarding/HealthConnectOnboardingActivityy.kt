package com.zenia.app.onboarding

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

class HealthConnectOnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenIATheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Conecta con Health Connect",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "ZenIA se integra con Health Connect para sincronizar tus datos de salud.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        finish()
    }
}