package com.zenia.app.ui.screens.zenia

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.screens.diary.DiarioViewModel

@Composable
fun ZeniaBotRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: ZeniaChatViewModel = hiltViewModel()
    val diarioViewModel: DiarioViewModel = hiltViewModel()


    val uiState by viewModel.uiState.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    val emergencyType by viewModel.emergencyType.collectAsState()
    val emergencyDisplay by viewModel.emergencyDisplay.collectAsState()

    val isPremium by viewModel.isPremium.collectAsState()
    val shareHealthData by viewModel.shareHealthData.collectAsState()

    val nickname by viewModel.nickname.collectAsState()

    val selectedDiaryDate by viewModel.selectedDiaryDate.collectAsState()
    val selectedDiaryEntry by viewModel.selectedDiaryEntry.collectAsState()

    val diarioUiState by diarioViewModel.uiState.collectAsState()

    var showDiaryPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.Builder().setLanguage("es").setRegion("MX").build()
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val onSpeakMessage: (String) -> Unit = { textToRead ->
        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            } else {
                it.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    ZeniaBotScreen(
        uiState = uiState,
        isTyping = isTyping,
        emergencyType = emergencyType,
        emergencyDisplay = emergencyDisplay,
        nickname = nickname,
        selectedDiaryDate = selectedDiaryDate,
        selectedDiaryEntry = selectedDiaryEntry,
        onClearSelectedEntry = { viewModel.limpiarEntradaSeleccionada() },
        onOpenDiaryPicker = { showDiaryPicker = true },
        onSendMessage = { viewModel.enviarMensaje(it) },
        onClearChat = { viewModel.eliminarHistorial() },
        onDeleteSelected = { ids -> viewModel.eliminarMensajesSeleccionados(ids) },
        onDismissBanner = { viewModel.dismissBannerToIcon() },
        onRestoreBanner = { viewModel.restoreBanner() },
        onNavigateBack = onNavigateBack,
        isPremium = isPremium,
        shareHealthData = shareHealthData,
        onToggleShareHealthData = { viewModel.toggleHealthDataSharing(it) },
        onSpeakMessage = onSpeakMessage
    )

    if (showDiaryPicker) {
        DiaryPickerBottomSheet(
            diarioUiState = diarioUiState,
            onDismiss = {
                showDiaryPicker = false
            },
            onDateSelected = { date ->
                viewModel.seleccionarFechaDiario(date.toString())
                showDiaryPicker = false
            },
            onYearChange = { increment ->
                diarioViewModel.changeYear(increment)
            },
            onJumpToToday = {
                diarioViewModel.jumpToToday()
            },
            onScrollConsumed = {
                diarioViewModel.resetScrollTarget()
            }
        )
    }
}