package com.zenia.app.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenia.app.R
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.util.DevicePreviews

@Composable
fun LockScreen(
    onUnlockClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_fingerprint),
                            contentDescription = stringResource(R.string.lock_content_description_fingerprint),
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

                    Text(
                        text = stringResource(R.string.lock_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                    Text(
                        text = stringResource(R.string.lock_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onUnlockClick,
                        modifier = Modifier
                            .widthIn(max = dimensions.buttonMaxWidth)
                            .fillMaxWidth()
                            .heightIn(min = dimensions.buttonHeight),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.lock_btn_unlock),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                    Text(
                        text = stringResource(R.string.lock_footer_trouble),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.heightIn(min = dimensions.buttonHeight)
                    ) {
                        Text(
                            text = stringResource(R.string.lock_btn_sign_out),
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun LockScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Compact) {
        LockScreen(
            onUnlockClick = {},
            onSignOut = {}
        )
    }
}

@DevicePreviews
@Composable
private fun LockScreenTabletPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        LockScreen(
            onUnlockClick = {},
            onSignOut = {}
        )
    }
}