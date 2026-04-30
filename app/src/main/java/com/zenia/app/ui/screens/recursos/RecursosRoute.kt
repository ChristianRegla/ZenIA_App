package com.zenia.app.ui.screens.recursos

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.util.RewardedAdHelper

@Composable
fun RecursosRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val recursosViewModel: RecursosViewModel = hiltViewModel()

    val isPremium by recursosViewModel.isPremium.collectAsState()
    val uiState by recursosViewModel.uiState.collectAsState()
    val isAdLoading by recursosViewModel.isAdLoading.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    val adHelper = remember { RewardedAdHelper(context) }
    LaunchedEffect(Unit) {
        adHelper.loadAd()
    }

    RecursosScreen(
        uiState = uiState,
        isUserPremium = isPremium,
        isAdLoading = isAdLoading,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToPremium = onNavigateToPremium,
        onToggleFavorite = { id, status -> recursosViewModel.toggleFavorite(id, status) },
        onRetry = { recursosViewModel.cargarRecursos() },
        onShowRewardedAd = { recursoId ->
            if (activity != null) {
                recursosViewModel.setAdLoadingState(true)
                adHelper.showAd(
                    activity = activity,
                    onRewardEarned = {
                        recursosViewModel.unlockResourceTemporarily(recursoId)
                    },
                    onAdDismissed = {
                        recursosViewModel.setAdLoadingState(false)
                    }
                )
            }
        }
    )
}