package com.zenia.app.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Apoya a ZenIA", // Usa stringResource en prod
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = ZeniaTeal,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tu apoyo mantiene a ZenIA viva",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFlex,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Somos un equipo pequeño de estudiantes. Cada donación nos ayuda a pagar los servidores y seguir mejorando.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontFamily = RobotoFlex
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- Tarjetas de Donación ---
            item {
                DonationCard(
                    icon = Icons.Default.Coffee,
                    title = "Invítanos un Café",
                    price = "$15.00 MXN",
                    color = Color(0xFF795548), // Café
                    onClick = { activity?.let { viewModel.donar(it) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DonationCard(
                    icon = Icons.Default.LocalPizza,
                    title = "Una Rebanada de Pizza",
                    price = "$45.00 MXN",
                    color = Color(0xFFFF9800), // Naranja
                    onClick = { activity?.let { viewModel.donar(it) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DonationCard(
                    icon = Icons.Default.Favorite,
                    title = "Patrocinador de Amor",
                    price = "$100.00 MXN",
                    color = Color(0xFFE91E63), // Rosa
                    isHighlight = true,
                    onClick = { activity?.let { viewModel.donar(it) } }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DonationCard(
    icon: ImageVector,
    title: String,
    price: String,
    color: Color,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isHighlight) color else Color.LightGray.copy(alpha = 0.5f)
    val borderWidth = if (isHighlight) 2.dp else 1.dp
    val containerColor = if (isHighlight) color.copy(alpha = 0.05f) else Color.White

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        color = containerColor,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = RobotoFlex)
                Text(text = "Pago único", fontSize = 12.sp, color = Color.Gray, fontFamily = RobotoFlex)
            }
            Text(
                text = price,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) color else Color.Black,
                fontFamily = RobotoFlex
            )
        }
    }
}