package com.zenia.app.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaTeal
import com.zenia.app.util.DevicePreviews
import kotlinx.coroutines.launch

enum class ChangeType(@param:StringRes val labelRes: Int, val icon: ImageVector, val color: Color) {
    FEATURE(R.string.changelog_type_feature, Icons.Default.RocketLaunch, Color(0xFFE8F5E9)),
    IMPROVEMENT(R.string.changelog_type_improvement, Icons.Default.AutoAwesome, Color(0xFFE3F2FD)),
    FIX(R.string.changelog_type_fix, Icons.Default.Build, Color(0xFFFBE9E7))
}

data class ChangeItem(
    @param:StringRes val textRes: Int,
    val type: ChangeType
)

data class ChangelogRelease(
    val version: String,
    @param:StringRes val dateRes: Int,
    val changes: List<ChangeItem>
)

@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit
) {
    val dimensions = ZenIATheme.dimensions

    val releaseHistory = listOf(
        ChangelogRelease(
            version = "v1.4.2",
            dateRes = R.string.changelog_1_4_2_date,
            changes = listOf(
                ChangeItem(R.string.changelog_1_4_2_feature_1, ChangeType.FEATURE),
                ChangeItem(R.string.changelog_1_4_2_improvement_1, ChangeType.IMPROVEMENT),
                ChangeItem(R.string.changelog_1_4_2_fix_1, ChangeType.FIX),
                ChangeItem(R.string.changelog_1_4_2_fix_2, ChangeType.FIX)
            )
        ),
        ChangelogRelease(
            version = "v1.4.0",
            dateRes = R.string.changelog_1_4_0_date,
            changes = listOf(
                ChangeItem(R.string.changelog_1_4_0_feature_1, ChangeType.FEATURE),
                ChangeItem(R.string.changelog_1_4_0_improvement_1, ChangeType.IMPROVEMENT),
                ChangeItem(R.string.changelog_1_4_0_improvement_2, ChangeType.IMPROVEMENT)
            )
        ),
        ChangelogRelease(
            version = "v1.3.0",
            dateRes = R.string.changelog_1_3_0_date,
            changes = listOf(
                ChangeItem(R.string.changelog_1_3_0_feature_1, ChangeType.FEATURE),
                ChangeItem(R.string.changelog_1_3_0_feature_2, ChangeType.FEATURE),
                ChangeItem(R.string.changelog_1_3_0_improvement, ChangeType.IMPROVEMENT)
            )
        ),
        ChangelogRelease(
            version = "v1.2.0",
            dateRes = R.string.changelog_1_2_0_date,
            changes = listOf(
                ChangeItem(R.string.changelog_1_2_0_feature, ChangeType.FEATURE),
                ChangeItem(R.string.changelog_1_2_0_fix, ChangeType.FIX)
            )
        ),
        ChangelogRelease(
            version = "v1.1.5",
            dateRes = R.string.changelog_1_1_5_date,
            changes = listOf(
                ChangeItem(R.string.changelog_1_1_5_improvement, ChangeType.IMPROVEMENT),
                ChangeItem(R.string.changelog_1_1_5_fix, ChangeType.FIX)
            )
        )
    )

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.changelog_title),
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = ZeniaLightGrey
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
            ) {
                items(releaseHistory) { release ->
                    ReleaseCard(release = release)
                }

                item {
                    Text(
                        text = stringResource(R.string.changelog_footer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensions.paddingMedium, bottom = dimensions.paddingExtraLarge),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ReleaseCard(release: ChangelogRelease) {
    val dimensions = ZenIATheme.dimensions

    val isPreview = LocalInspectionMode.current

    val alpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val translateY = remember { Animatable(if (isPreview) 0f else 100f) }

    LaunchedEffect(release.version) {
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            translateY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationY = translateY.value
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(dimensions.cornerRadiusNormal),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.paddingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = release.version,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ZeniaTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(release.dateRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    softWrap = false
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = dimensions.paddingMedium),
                thickness = DividerDefaults.Thickness,
                color = ZeniaLightGrey
            )

            Column(verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)) {
                release.changes.forEach { change ->
                    ChangeItemRow(change = change)
                }
            }
        }
    }
}

@Composable
fun ChangeItemRow(change: ChangeItem) {
    val dimensions = ZenIATheme.dimensions

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(change.type.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = change.type.icon,
                contentDescription = stringResource(change.type.labelRes),
                tint = Color.DarkGray.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(dimensions.paddingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(change.type.labelRes),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(change.textRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun ChangelogScreenPreview() {
    ZenIATheme(windowSizeClass = WindowWidthSizeClass.Expanded) {
        ChangelogScreen(
            onNavigateBack = {}
        )
    }
}