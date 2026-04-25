package com.zenia.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.data.CommunityRepository
import com.zenia.app.data.ContentRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.CommunityPost
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    contentRepository: ContentRepository,
    private val diaryRepository: DiaryRepository,
    private val communityRepository: CommunityRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    val userName = sessionManager.nickname

    private val sevenDaysAgo = LocalDate.now().minusDays(7).toString()

    val registrosDiario = diaryRepository.getEntriesFromDate(sevenDaysAgo)
        .onEach { entradas ->
            processChartData(entradas)
            loadStreak()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasEntryToday: StateFlow<Boolean> = registrosDiario.map { lista ->
        val todayStr = LocalDate.now().toString()
        lista.any { it.fecha == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val chartProducer = ChartEntryModelProducer()

    val moodInsights = diaryRepository.getEntriesFromDate(
        LocalDate.now().minusDays(30).toString()
    ).map { entries ->
        AnalysisUtils.analyzePatterns(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()

    private val _trendingPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val trendingPosts = _trendingPosts.asStateFlow()

    init {
        cargarPostDestacados()
        observarActualizacionesDePosts()
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private fun loadStreak() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId
            if (userId != null) {
                try {
                    val streak = diaryRepository.calculateCurrentStreak()
                    _currentStreak.value = streak
                } catch (e: Exception) {
                    e.printStackTrace()
                    _currentStreak.value = 0
                }
            }
        }
    }

    private fun processChartData(registros: List<DiarioEntrada>) {
        val entries = registros
            .filter { !it.estadoAnimo.isNullOrBlank() }
            .mapNotNull { entrada ->
                try {
                    val date = LocalDate.parse(entrada.fecha)
                    val xValue = date.toEpochDay().toFloat()
                    val moodValue = ChartUtils.mapMoodToValue(entrada.estadoAnimo)

                    if (moodValue > 0f) {
                        entryOf(xValue, moodValue)
                    } else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            .sortedBy { it.x }
            .takeLast(7)

        chartProducer.setEntries(entries)
    }

    fun cargarPostDestacados() {
        viewModelScope.launch {
            try {
                val currentUserId = sessionManager.currentUserId ?: return@launch

                val blockedUserResult = communityRepository.getBlockedUsers(currentUserId)
                val blockedUserIds = blockedUserResult.getOrNull() ?: emptyList()

                val (posts, _) = communityRepository.getPosts(
                    lastVisible = null,
                    limit = 10,
                    currentUserId = sessionManager.currentUserId
                )

                val filteredPosts = posts.filter { post ->
                    post.authorId !in blockedUserIds
                }.take(5)

                _trendingPosts.value = filteredPosts
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observarActualizacionesDePosts() {
        viewModelScope.launch {
            communityRepository.postUpdates.collect { updatedPost ->
                _trendingPosts.update { currentList ->
                    currentList.map { if (it.id == updatedPost.id) updatedPost else it }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}