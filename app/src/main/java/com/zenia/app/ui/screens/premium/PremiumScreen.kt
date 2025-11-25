package com.zenia.app.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.Nunito
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaPremiumPurple
import com.zenia.app.ui.theme.ZeniaSlateGrey

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit
) {
    ZenIATheme {
        Scaffold(
            topBar = {
                ZeniaTopBar(
                    title = "Suscribirse",
                    onNavigateBack = onNavigateBack,
                    containerColor = ZeniaPremiumPurple,
                    contentColor = Color.White
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                PlanCard(
                    title = "ZenIA Ilimitado",
                    subtitle = "Para quienes buscan un cambio profundo.",
                    price = "$75 MXN/Mes",
                    buttonText = "Obtener beneficio",
                    isPremium = true,
                    features = listOf(
                        "Integración con Smartwatch",
                        "Análisis Avanzado",
                        "Eliminación de anuncios",
                        "Acceso total a la biblioteca de ejercicios de relajación",
                        "Acceso total a todos los recursos educativos"
                    ),
                    onClick = {  }
                )

                PlanCard(
                    title = "ZenIA Esencial",
                    subtitle = "Ideal para empezar a conocerte",
                    price = "$0 MXN/Mes",
                    buttonText = "Continuar",
                    isPremium = false,
                    features = listOf(
                        "Seguimiento del estado de ánimo diario",
                        "Acceso parcial a la biblioteca de ejercicios de relajación",
                        "Interacción con el chatbot de IA",
                        "Acceso parcial a todos los recursos educativos y la línea de ayuda"
                    ),
                    onClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    buttonText: String,
    isPremium: Boolean,
    features: List<String>,
    onClick: () -> Unit
) {
    val cardBackgroundColor = if (isPremium) Color(0xFFFDF2FF) else Color.White
    val borderColor = if (isPremium) ZeniaPremiumPurple else Color.LightGray
    val titleBackgroundColor = if (isPremium) ZeniaPremiumPurple else Color(0xFF9CA3AF)
    val buttonColor = if (isPremium) ZeniaPremiumPurple else Color(0xFF9CA3AF)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPremium) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPremium) 8.dp else 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(titleBackgroundColor)
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = ZeniaSlateGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = price,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 10.dp)
            ) {
                Text(
                    text = buttonText,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )

                        Text(
                            text = feature,
                            fontFamily = Nunito,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 8.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}