package com.zenia.app.ui.screens.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zenia.app.ui.components.shimmerEffect

@Composable
fun CalendarSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
            Row {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).shimmerEffect())
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).shimmerEffect())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Simular la cuadrícula de días (6 semanas aprox)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { // 6 filas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Cuadrados
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(120.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .shimmerEffect()
        )
    }
}