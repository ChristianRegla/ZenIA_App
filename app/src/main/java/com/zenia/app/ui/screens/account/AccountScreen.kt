package com.zenia.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme

data class AccountScreenState(
    val isLoading: Boolean,
    val userEmail: String,
    val isVerified: Boolean,
    val showDeleteDialog: Boolean,
    val snackbarHostState: SnackbarHostState
)

data class AccountScreenActions(
    val onNavigateBack: () -> Unit,
    val onResendVerification: () -> Unit,
    val onChangePassword: () -> Unit,
    val onDeleteAccountRequest: () -> Unit,
    val onConfirmDeleteAccount: () -> Unit,
    val onDismissDeleteDialog: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountScreenState,
    actions: AccountScreenActions
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.account_title),
                onNavigateBack = actions.onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.account_info_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Info del Usuario
            InfoItem(
                label = stringResource(R.string.account_email_label),
                value = state.userEmail.ifEmpty { stringResource(R.string.common_not_available) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoItem(
                label = stringResource(R.string.account_verified_label),
                value = stringResource(if (state.isVerified) R.string.common_yes else R.string.common_no),
                isPositive = state.isVerified
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = actions.onChangePassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.account_change_password_button))
                }

                if (!state.isVerified) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = actions.onResendVerification,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.account_resend_verification_button))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = actions.onDeleteAccountRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.account_delete_button))
                }

                Text(
                    text = stringResource(R.string.account_delete_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = actions.onDismissDeleteDialog,
            title = { Text(stringResource(R.string.account_delete_dialog_title)) },
            text = { Text(stringResource(R.string.account_delete_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = actions.onConfirmDeleteAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDeleteDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    isPositive: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isPositive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    ZenIATheme {
        AccountScreen(
            state = AccountScreenState(
                isLoading = false,
                userEmail = "usuario@zenia.com",
                isVerified = false,
                showDeleteDialog = false,
                snackbarHostState = SnackbarHostState()
            ),
            actions = AccountScreenActions({}, {}, {}, {}, {}, {})
        )
    }
}