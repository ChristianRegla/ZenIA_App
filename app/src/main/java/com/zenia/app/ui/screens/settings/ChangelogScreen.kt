package com.zenia.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zenia.app.ui.components.ZeniaTopBar
import com.zenia.app.ui.theme.ZeniaLightGrey

@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = "Changelog",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = ZeniaLightGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

        }
    }
}