package com.zenia.app.ui.screens.zenia

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zenia.app.R
import com.zenia.app.ui.theme.ZeniaLightGrey
import com.zenia.app.ui.theme.ZeniaTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun EmergencyTopBanner(
    triggerType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPhysical = triggerType == "physical_risk"
    val backgroundColor = ZeniaLightGrey
    val contentColor = Color.Black

    val icon = if (isPhysical) Icons.Default.LocalHospital else Icons.Default.Favorite
    val title = if (isPhysical) stringResource(R.string.emergency_medical_title) else stringResource(R.string.emergency_mental_title)
    val message = if (isPhysical) stringResource(R.string.emergency_medical_desc) else stringResource(R.string.emergency_mental_desc)
    val buttonText = if (isPhysical) stringResource(R.string.emergency_call_911) else stringResource(R.string.emergency_call_lifeline)

    var borderWidth by remember { mutableStateOf(0.dp) }

    val animatedBorderWidth by animateDpAsState(
        targetValue = borderWidth,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "borderWidthAnim"
    )

    LaunchedEffect(Unit) {
        borderWidth = 3.dp
        delay(600)
        borderWidth = 0.5.dp
    }

    val baseBorderColor = if (isPhysical) MaterialTheme.colorScheme.error else ZeniaTeal
    val borderColor = baseBorderColor.copy(alpha = if (animatedBorderWidth > 1.dp) 0.8f else 0.3f)

    var offsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = borderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .offset {
                IntOffset(x = 0, y = offsetY.roundToInt())
            }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (delta < 0 || offsetY < 0) {
                        offsetY += delta
                    }
                },
                onDragStopped = { velocity ->
                    if (offsetY < -150f || velocity < -500f) {
                        scope.launch {
                            animate(initialValue = offsetY, targetValue = -500f) { value, _ ->
                                offsetY = value
                            }
                            onDismiss()
                        }
                    } else {
                        scope.launch {
                            animate(initialValue = offsetY, targetValue = 0f) { value, _ ->
                                offsetY = value
                            }
                        }
                    }
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.minimize),
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:${if (isPhysical) "911" else "8009112000"}".toUri())
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPhysical) MaterialTheme.colorScheme.error else ZeniaTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonText)
            }
        }
    }
}