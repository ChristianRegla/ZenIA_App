package com.zenia.app.ui.screens.onboarding

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.R
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaPremiumBackground
import com.zenia.app.ui.theme.ZeniaPremiumPurple
import java.util.Locale

@Composable
fun OnboardingRoute(
    onNavigateToAuth: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {

    val currentLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    val currentLanguage = currentLocale.language

    val onFinish = {
        viewModel.completeOnboarding()
        onNavigateToAuth()
    }

    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_welcome),
            description = stringResource(R.string.onboarding_desc_welcome),
            iconRes = R.drawable.ic_nube_feli,
            color = ZeniaFeelings
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_diary),
            description = stringResource(R.string.onboarding_desc_diary),
            lottieRes = R.raw.notepad,
            color = ZeniaPremiumBackground
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_chat),
            description = stringResource(R.string.onboarding_desc_chat),
            lottieRes = R.raw.chatbot_animation,
            color = ZeniaPremiumPurple
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_resources),
            description = stringResource(R.string.onboarding_desc_resources),
            lottieRes = R.raw.breathe,
            color = ZeniaExercise
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_biometrics),
            description = stringResource(R.string.onboarding_desc_biometrics),
            lottieRes = R.raw.biometrics,
            color = Color(0xFF69E56E)
        )
    )

    OnboardingScreen(
        currentLanguage = currentLanguage,
        pages = pages,
        onLanguageChange = { newLanguage ->
            val appLocale = LocaleListCompat.forLanguageTags(newLanguage)
            AppCompatDelegate.setApplicationLocales(appLocale)
        },
        onFinish = onFinish
    )
}