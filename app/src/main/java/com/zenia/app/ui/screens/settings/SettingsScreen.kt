package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaInputLabel
import com.zenia.app.ui.theme.ZeniaSlateGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    name: String,
    email: String,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMoreSettings: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onSignOut: () -> Unit
) {
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- FOTO DE PERFIL ---
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.settings_profile_picture_desc),
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }

                    // --- NOMBRE Y EMAIL ---
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onNavigateToProfile)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = name,
                                fontFamily = RobotoFlex,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                            Text(
                                text = email,
                                fontFamily = RobotoFlex,
                                fontSize = 14.sp,
                                color = ZeniaSlateGrey
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Ir al perfil",
                            tint = ZeniaInputLabel
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- BOTÓN PREMIUM ---
                    Button(
                        onClick = onNavigateToPremium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD946EF)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_btn_premium),
                            fontFamily = RobotoFlex,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho del padre (600dp)
                    ) {
                        SettingsItem(
                            iconRes = R.drawable.ic_settings,
                            text = stringResource(R.string.settings_item_settings),
                            onClick = onNavigateToMoreSettings
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_help_center,
                            text = stringResource(R.string.settings_item_help_center),
                            onClick = onNavigateToHelp
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_donations,
                            text = stringResource(R.string.settings_item_donations),
                            onClick = onNavigateToDonations
                        )
                        SettingsDivider()
                        SettingsItem(
                            iconRes = R.drawable.ic_privacy_policy,
                            text = stringResource(R.string.settings_item_privacy),
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
            tint = ZeniaSlateGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontFamily = RobotoFlex,
            fontSize = 16.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ZeniaInputLabel
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 0.5.dp,
        color = Color.LightGray.copy(alpha = 0.5f)
    )
}

@Preview(name = "Phone", showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420")
@Composable
fun SettingsPhonePreview() {
    ZenIATheme {
        SettingsScreen(
            name = "John Doe",
            email = "john.doe@example.com",
            {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}