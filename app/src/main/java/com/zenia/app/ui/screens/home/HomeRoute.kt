package com.zenia.app.ui.screens.home

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.navigation.Destinations
import java.time.LocalDate

/**
 * Composable "inteligente" (Smart Composable) para la ruta principal (Home).
 * Obtiene el estado de [HomeViewModel], maneja la lógica de Health Connect
 * y pasa el estado y las acciones a [HomeScreen].
 */
@Composable
fun HomeRoute(
    onNavigateToSettings: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToDiaryEntry: (LocalDate) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCommunity: () -> Unit
) {
    val homeViewModel: HomeViewModel = hiltViewModel()

    val uiState by homeViewModel.uiState.collectAsState()
    val registros by homeViewModel.registrosDiario.collectAsState()
    val userName by homeViewModel.userName.collectAsState()
    val hasEntryToday by homeViewModel.hasEntryToday.collectAsState()
    val communityActivities by homeViewModel.communityActivities.collectAsState()
    val currentStreak by homeViewModel.currentStreak.collectAsState()
    val moodInsights by homeViewModel.moodInsights.collectAsState()

    HomeScreen(
        uiState = uiState,
        userName = userName,
        registrosDiario = registros,
        hasEntryToday = hasEntryToday,
        communityActivities = communityActivities,
        chartProducer = homeViewModel.chartProducer,
        onNavigateToDiaryEntry = onNavigateToDiaryEntry,
        onSettingsClick = onNavigateToSettings,
        onNotificationClick = onNotificationClick,
        onResetState = { homeViewModel.resetState() },
        onNavigateToSOS = onNavigateToSOS,
        currentStreak = currentStreak,
        topBooster = moodInsights.first,
        topDrainer = moodInsights.second,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToCommunity = onNavigateToCommunity
    )
}