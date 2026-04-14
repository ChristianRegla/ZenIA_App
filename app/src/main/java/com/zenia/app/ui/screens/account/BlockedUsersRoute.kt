package com.zenia.app.ui.screens.account

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.model.BlockedUserProfile
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal

@Composable
fun BlockedUsersRoute(
    onNavigateBack: () -> Unit,
    viewModel: BlockedUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado para controlar el diálogo de confirmación
    var userToUnblock by remember { mutableStateOf<BlockedUserProfile?>(null) }

    LaunchedEffect(uiState.actionMessage, uiState.error) {
        uiState.actionMessage?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(it, SnackbarState.SUCCESS))
            viewModel.clearMessages()
        }
        uiState.error?.let {
            ZeniaSnackbarController.showMessage(ZeniaSnackbarData(it, SnackbarState.ERROR))
            viewModel.clearMessages()
        }
    }

    // Diálogo de confirmación
    if (userToUnblock != null) {
        AlertDialog(
            onDismissRequest = { userToUnblock = null },
            title = {
                Text(
                    text = "Desbloquear usuario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas desbloquear a este usuario? Volverás a ver sus publicaciones en la comunidad.",
                    color = ZeniaSlateGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToUnblock?.let { viewModel.unblockUser(it) }
                        userToUnblock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Desbloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToUnblock = null }) {
                    Text("Cancelar", color = ZeniaSlateGrey)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    BlockedUsersScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUnblockClick = { user -> userToUnblock = user } // Abrimos el diálogo en lugar de ejecutar la acción directa
    )
}