package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaSlateGrey
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.ProfanityFilter

val availableAvatars = listOf(
    R.drawable.avatar_1,
    R.drawable.avatar_2,
    R.drawable.avatar_3,
    R.drawable.avatar_4,
    R.drawable.avatar_5
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    name: String?,
    email: String?,
    avatarIndex: Int,
    isPremium: Boolean,
    onUpdateProfile: (String, Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMoreSettings: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onSignOut: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    ZenIATheme {
        Scaffold(
            topBar = {
                ZeniaTopBar(
                    title = stringResource(R.string.settings_title),
                    onNavigateBack = onNavigateBack
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileHeader(
                        nickname = name,
                        email = email,
                        avatarIndex = avatarIndex,
                        onClick = { showEditDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- BOTÓN PREMIUM ---
                    Button(
                        onClick = onNavigateToPremium,
                        colors = if (isPremium) ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD946EF)) else ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD946EF)
                        ),
                        shape = RoundedCornerShape(50),
                        border = if (isPremium) BorderStroke(1.dp, Color(0xFFD946EF)) else null,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        if (isPremium) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isPremium) "Plan Premium Activo" else stringResource(R.string.settings_btn_premium),
                            fontFamily = RobotoFlex,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_settings,
                            text = stringResource(R.string.settings_item_settings),
                            textColor = ZeniaSlateGrey,
                            onClick = onNavigateToMoreSettings
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_help_center,
                            text = stringResource(R.string.settings_item_help_center),
                            textColor = ZeniaSlateGrey,
                            onClick = onNavigateToHelp
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_donations,
                            text = stringResource(R.string.settings_item_donations),
                            textColor = ZeniaSlateGrey,
                            onClick = onNavigateToDonations
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_privacy_policy,
                            text = stringResource(R.string.settings_item_privacy),
                            textColor = ZeniaSlateGrey,
                            onClick = onNavigateToPrivacy
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_logout,
                            text = stringResource(R.string.settings_item_logout),
                            textColor = Color.Red.copy(alpha = 0.8f),
                            showArrow = false,
                            onClick = onSignOut
                        )
                        SettingsDivider()
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        if (showEditDialog) {
            EditProfileDialog(
                currentNickname = name ?: "",
                currentAvatarIndex = avatarIndex,
                onDismiss = { showEditDialog = false },
                onSave = { newName, newAvatarIdx ->
                    onUpdateProfile(newName, newAvatarIdx)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    nickname: String?,
    email: String?,
    avatarIndex: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (avatarIndex in availableAvatars.indices) {
                    Image(
                        painter = painterResource(id = availableAvatars[avatarIndex]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ZeniaTeal)
                    .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!nickname.isNullOrEmpty()) {
            Text(
                text = nickname,
                fontFamily = RobotoFlex,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
        } else {
            Text(
                text = stringResource(R.string.profile_set_nickname),
                fontFamily = RobotoFlex,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = ZeniaTeal
            )
        }

        if (email != null) {
            Text(
                text = email,
                fontFamily = RobotoFlex,
                fontSize = 14.sp,
                color = ZeniaSlateGrey
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentNickname: String,
    currentAvatarIndex: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }
    var selectedAvatarIdx by remember { mutableIntStateOf(currentAvatarIndex) }

    var isError by remember { mutableStateOf(false) }
    val maxChar = 20

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.profile_edit_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Avatar
                Text(
                    text = stringResource(R.string.profile_select_avatar),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(150.dp)
                ) {
                    itemsIndexed(availableAvatars) { index, drawableRes ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    width = if (selectedAvatarIdx == index) 3.dp else 0.dp,
                                    color = if (selectedAvatarIdx == index) ZeniaTeal else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatarIdx = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = drawableRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        if (it.length <= maxChar){
                            nickname = it
                            isError = ProfanityFilter.hasProfanity(it)
                        }
                    },
                    label = { Text(stringResource(R.string.profile_nickname_label)) },
                    placeholder = { Text(stringResource(R.string.profile_nickname_hint)) },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Elige un apodo respetuoso para la comunidad",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_community_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Text(
                                    text = "${nickname.length}/$maxChar",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.profile_cancel), color = ZeniaSlateGrey)
                    }
                    Button(
                        onClick = {
                            if (!ProfanityFilter.hasProfanity(nickname)) {
                                onSave(nickname.trim(), selectedAvatarIdx)
                            } else {
                                isError = true
                            }
                        },
                        enabled = nickname.isNotBlank() && !isError,
                        colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                    ) {
                        Text(stringResource(R.string.profile_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    iconRes: Int,
    text: String,
    textColor: Color = Color.Black,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontFamily = RobotoFlex,
            fontSize = 16.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (showArrow) ZeniaSlateGrey else Color.Transparent,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 0.5.dp,
        color = ZeniaSlateGrey
    )
}

@Preview(name = "Phone", showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420")
@Composable
fun SettingsPhonePreview() {
    ZenIATheme {
        SettingsScreen(
            name = null,
            email = "john.doe@example.com",
            avatarIndex = -1,
            isPremium = false,
            onUpdateProfile = { _, _ -> },
            {}, {}, {}, {}, {}, {}, {},
            onSignOut = {}
        )
    }
}