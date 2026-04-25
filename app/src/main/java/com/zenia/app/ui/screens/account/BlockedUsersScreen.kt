package com.zenia.app.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zenia.app.R
import com.zenia.app.model.BlockedUserProfile
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.screens.community.UserAvatar
import com.zenia.app.ui.theme.ZeniaDark
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal

@Composable
fun BlockedUsersScreen(
    uiState: BlockedUsersViewModel.UiState,
    onNavigateBack: () -> Unit,
    onUnblockClick: (BlockedUserProfile) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = { ZeniaTopBar(title = stringResource(R.string.title_blocked_users), onNavigateBack = onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ZeniaTeal
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))

                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(250.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.error_connection_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.error_connection_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                        ) {
                            Text(
                                text = stringResource(R.string.retry),
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                uiState.blockedUsers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.empty_list),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.empty_blocked_users_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZeniaDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.empty_blocked_users_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZeniaSlateGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        LazyColumn {
                            itemsIndexed(uiState.blockedUsers) { index, user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(user.avatarIndex, user.isPremium, 40.dp)

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        text = user.apodo,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Medium,
                                        color = ZeniaDark
                                    )

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
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}