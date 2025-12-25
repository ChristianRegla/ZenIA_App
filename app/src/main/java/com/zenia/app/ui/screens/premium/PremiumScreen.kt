package com.zenia.app.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Escuchamos el estado real
    val isPremium by viewModel.isUserPremium.collectAsState()

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Suscribirse",
                onNavigateBack = onNavigateBack,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono Hero
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFFFC107)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isPremium) "¡Ya eres Premium!" else "Desbloquea todo el potencial",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = RobotoFlex
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Lista de Beneficios
                PremiumBenefitItem("Análisis ilimitados de diario")
                PremiumBenefitItem("Sincronización con Smartwatch")
                PremiumBenefitItem("Acceso completo a ejercicios")
                PremiumBenefitItem("Sin límites de chat con la IA")

                Spacer(modifier = Modifier.height(48.dp))

                // --- ZONA DE GESTIÓN O COMPRA ---
                if (isPremium) {
                    // VISTA PARA USUARIOS PREMIUM (Gestión)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ZeniaTeal.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZeniaTeal)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = ZeniaTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Suscripción Activa",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ZeniaTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tu próxima renovación se gestiona directamente en Google Play Store.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón de Cancelar / Gestionar
                            OutlinedButton(
                                onClick = { activity?.let { viewModel.gestionarSuscripcion(it) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Gestionar o Cancelar Suscripción")
                            }
                        }
                    }
                } else {
                    // VISTA DE VENTA (No Premium)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Plan Anual",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$29.00 / año",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Se cobrará anualmente. Cancela cuando quieras.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { activity?.let { viewModel.comprarPremium(it) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Suscribirse Ahora")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumBenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontFamily = RobotoFlex)
    }
}