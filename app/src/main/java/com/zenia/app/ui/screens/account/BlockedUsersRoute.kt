package com.zenia.app.ui.screens.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.components.SnackbarState
import com.zenia.app.ui.components.ZeniaSnackbarController
import com.zenia.app.ui.components.ZeniaSnackbarData

@Composable
fun BlockedUsersRoute(
    onNavigateBack: () -> Unit,
    viewModel: BlockedUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

    BlockedUsersScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUnblockClick = { viewModel.unblockUser(it) }
    )
}