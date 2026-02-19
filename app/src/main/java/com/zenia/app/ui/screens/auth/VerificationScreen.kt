package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.theme.Lobster
import com.zenia.app.ui.theme.Nunito

@Composable
fun VerificationScreen(
    email: String,
    onResendClick: () -> Unit,
    onCancelClick: () -> Unit,
    resendTimer: Int,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_login_signup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.1f) // Muy sutil
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Ícono Animado o Imagen de "Nube Feliz" esperando
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nube_feli), // Tu ícono característico
                    contentDescription = "Esperando verificación",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Título Moderno
            Text(
                text = "¡Revisa tu correo!",
                fontFamily = Lobster,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Descripción clara
            Text(
                text = "Hemos enviado un enlace de confirmación a:\n",
                fontFamily = Nunito,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "Toca el enlace en ese correo para activar tu cuenta. \nEsta pantalla se actualizará automáticamente.",
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 4. Indicador de "Escuchando..."
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Esperando confirmación...",
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 5. Botones de Acción
            Button(
                onClick = onResendClick,
                enabled = resendTimer == 0 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (resendTimer > 0) "Reenviar en ${resendTimer}s" else "Reenviar correo",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cambiar correo / Salir",
                    fontFamily = Nunito,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (isLoading) {
            ZenLoadingOverlay()
        }
    }
}