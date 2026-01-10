package com.zenia.app.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingRoute(
    onNavigateToAuth: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val onFinish = {
        viewModel.completeOnboarding()
        onNavigateToAuth()
    }

    OnboardingScreen(
        onFinish = onFinish
    )
}