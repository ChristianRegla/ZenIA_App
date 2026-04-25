package com.zenia.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zenia.app.R
import com.zenia.app.ui.theme.Lobster
import com.zenia.app.ui.theme.Nunito
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.util.DevicePreviews

@Composable
fun VerificationScreen(
    email: String,
    onResendClick: () -> Unit,
    onCancelClick: () -> Unit,
    resendTimer: Int,
    isLoading: Boolean,
    isResending: Boolean
) {
    val dimensions = ZenIATheme.dimensions

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_login_signup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.verification_mail)
                )

                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(100.dp)
                        .padding(dimensions.paddingSmall),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

            Text(
                text = stringResource(id = R.string.verification_title),
                fontFamily = Lobster,
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            Text(
                text = stringResource(id = R.string.verification_subtitle_1),
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = dimensions.paddingSmall)
            )

            Text(
                text = stringResource(id = R.string.verification_subtitle_2),
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = dimensions.paddingMedium)
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                    text = stringResource(id = R.string.verification_waiting),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onResendClick,
                enabled = resendTimer == 0 && !isResending,
                modifier = Modifier
                    .widthIn(max = dimensions.buttonMaxWidth)
                    .fillMaxWidth()
                    .heightIn(min = dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusNormal)
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (resendTimer > 0) {
                            stringResource(id = R.string.verification_resend_timer, resendTimer)
                        } else {
                            stringResource(id = R.string.verification_resend_btn)
                        },
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            TextButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .widthIn(max = dimensions.buttonMaxWidth)
                    .fillMaxWidth()
                    .heightIn(min = dimensions.buttonHeight)
            ) {
                Text(
                    text = stringResource(id = R.string.verification_cancel_btn),
                    fontFamily = Nunito,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isLoading) {
            ZenLoadingOverlay()
        }
    }
}

@DevicePreviews
@Composable
private fun VerificationScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        VerificationScreen(
            email = "usuario.nuevo@ejemplo.com",
            onResendClick = {},
            onCancelClick = {},
            resendTimer = 0,
            isLoading = false,
            isResending = false
        )
    }
}

@DevicePreviews
@Composable
private fun VerificationScreenTimerPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        VerificationScreen(
            email = "un.correo.extremadamente.largo.que.podria.romper.todo@empresa.com.mx",
            onResendClick = {},
            onCancelClick = {},
            resendTimer = 45,
            isLoading = false,
            isResending = false
        )
    }
}