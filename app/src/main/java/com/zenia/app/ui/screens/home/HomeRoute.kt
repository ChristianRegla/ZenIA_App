package com.zenia.app.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.model.CommunityPost
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
    onNavigateToCommunity: () -> Unit,
    onNavigateToPostDetail: (CommunityPost) -> Unit
) {
    val homeViewModel: HomeViewModel = hiltViewModel()

    val uiState by homeViewModel.uiState.collectAsState()
    val registros by homeViewModel.registrosDiario.collectAsState()
    val userName by homeViewModel.userName.collectAsState()
    val hasEntryToday by homeViewModel.hasEntryToday.collectAsState()
    val trendingPosts by homeViewModel.trendingPosts.collectAsState()
    val currentStreak by homeViewModel.currentStreak.collectAsState()
    val moodInsights by homeViewModel.moodInsights.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.cargarPostDestacados()
    }

    HomeScreen(
        uiState = uiState,
        userName = userName,
        registrosDiario = registros,
        hasEntryToday = hasEntryToday,
        trendingPosts = trendingPosts,
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
        onNavigateToCommunity = onNavigateToCommunity,
        onNavigateToPostDetail = onNavigateToPostDetail
    )
}