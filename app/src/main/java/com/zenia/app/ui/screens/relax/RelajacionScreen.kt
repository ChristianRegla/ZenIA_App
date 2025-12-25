package com.zenia.app.ui.screens.relax

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.zenia.app.R
import com.zenia.app.ui.components.ZeniaTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelajacionScreen() {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = stringResource(R.string.nav_relax),
                onNavigateBack = null,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🧘\nAquí irán los ejercicios\nde respiración y meditación.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}