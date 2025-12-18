package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.components.shimmerEffect

@Composable
fun CalendarSkeleton() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 450.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(150.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Row {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).shimmerEffect())
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).shimmerEffect())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) {
                    Box(modifier = Modifier.weight(1f).height(20.dp).padding(horizontal = 4.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(7) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(120.dp).height(48.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkeletonPreview() {
    CalendarSkeleton()
}