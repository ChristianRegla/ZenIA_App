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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaTeal
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
    val releaseHistory = listOf(
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(releaseHistory) { release ->
                ReleaseCard(release = release)
            }

            item {
                Text(
                    text = stringResource(R.string.changelog_footer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ReleaseCard(release: ChangelogRelease) {
    val alpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(100f) }

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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                    color = ZeniaTeal
                )
                Text(
                    text = stringResource(release.dateRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = ZeniaLightGrey
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                release.changes.forEach { change ->
                    ChangeItemRow(change = change)
                }
            }
        }
    }
}

@Composable
fun ChangeItemRow(change: ChangeItem) {
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

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = stringResource(change.type.labelRes),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = stringResource(change.textRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}