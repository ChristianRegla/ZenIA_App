package com.zenia.app.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenia.app.model.BlockedUserProfile
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.screens.community.UserAvatar
import com.zenia.app.ui.theme.ZeniaDark
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.R
import androidx.compose.ui.res.stringResource

@Composable
fun BlockedUsersScreen(
    uiState: BlockedUsersViewModel.UiState,
    onNavigateBack: () -> Unit,
    onUnblockClick: (BlockedUserProfile) -> Unit
) {
    Scaffold(
        topBar = { ZeniaTopBar(title = stringResource(R.string.title_blocked_users), onNavigateBack = onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = ZeniaTeal)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn {
                        itemsIndexed(uiState.blockedUsers) { index, user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(user.avatarIndex, user.isPremium, 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(user.apodo, Modifier.weight(1f), fontWeight = FontWeight.Medium, color = ZeniaDark)

                                OutlinedButton(
                                    onClick = { onUnblockClick(user) },
                                    border = BorderStroke(1.dp, ZeniaTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(stringResource(R.string.action_unblock), color = ZeniaTeal)
                                }
                            }
                            if (index < uiState.blockedUsers.size - 1) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}