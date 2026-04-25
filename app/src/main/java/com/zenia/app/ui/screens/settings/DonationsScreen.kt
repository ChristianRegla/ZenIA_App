package com.zenia.app.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.RobotoFlex
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal

enum class DonationOption {
    CAFE, PIZZA, AMOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationsScreen(
    onNavigateBack: () -> Unit,
    onDonateCafe: (Activity) -> Unit,
    onDonatePizza: (Activity) -> Unit,
    onDonateAmor: (Activity) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedOption by remember { mutableStateOf<DonationOption?>(null) }

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.donations_title),
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        activity?.let { act ->
                            when (selectedOption) {
                                DonationOption.CAFE -> onDonateCafe(act)
                                DonationOption.PIZZA -> onDonatePizza(act)
                                DonationOption.AMOR -> onDonateAmor(act)
                                null -> {}
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(50.dp),
                    enabled = selectedOption != null,
                    colors = ButtonDefaults.buttonColors(containerColor = ZeniaTeal)
                ) {
                    Text(
                        text = stringResource(R.string.donate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoFlex
                    )
                }
            }
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
                    text = stringResource(R.string.donations_header),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFlex,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.donations_description),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontFamily = RobotoFlex
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                DonationCard(
                    icon = Icons.Default.Coffee,
                    title = stringResource(R.string.donation_coffee),
                    price = "$15.00 MXN",
                    color = Color(0xFF795548),
                    isSelected = selectedOption == DonationOption.CAFE,
                    onClick = { selectedOption = DonationOption.CAFE }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DonationCard(
                    icon = Icons.Default.LocalPizza,
                    title = stringResource(R.string.donation_pizza),
                    price = "$45.00 MXN",
                    color = Color(0xFFFF9800),
                    isSelected = selectedOption == DonationOption.PIZZA,
                    onClick = { selectedOption = DonationOption.PIZZA }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DonationCard(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.donation_sponsor),
                    price = "$100.00 MXN",
                    color = Color(0xFFE91E63), // Rosa
                    isSelected = selectedOption == DonationOption.AMOR,
                    onClick = { selectedOption = DonationOption.AMOR }
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
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) color else Color.LightGray.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isSelected) color.copy(alpha = 0.05f) else Color.White

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                Text(text = stringResource(R.string.donation_one_time), fontSize = 12.sp, color = Color.Gray, fontFamily = RobotoFlex)
            }
            Text(
                text = price,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else Color.Black,
                fontFamily = RobotoFlex
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DonationsScreenPreview() {
    DonationsScreen(
        onNavigateBack = {},
        onDonateCafe = {},
        onDonatePizza = {},
        onDonateAmor = {}
    )
}