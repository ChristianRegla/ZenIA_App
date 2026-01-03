package com.zenia.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.zenia.app.ui.navigation.AppNavigation
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (intent.action == "androidx.health.connect.client.ACTION_SHOW_PERMISSIONS_RATIONALE") {
            AlertDialog.Builder(this)
                .setTitle(R.string.health_connect_rationale_title)
                .setMessage(R.string.health_connect_rationale_message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.privacy_policy) { _, _ ->
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, "https://tuzeniaapp.com/privacidad".toUri())
                    startActivity(browserIntent)
                }
                .setCancelable(false)
                .show()
        }

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.startDestinationState.value == null
        }

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->

            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )

            fadeOut.duration = 500L

            fadeOut.doOnEnd { splashScreenViewProvider.remove() }

            fadeOut.start()
        }

        val pendingDeepLink = intent?.data

        enableEdgeToEdge()
        setContent {
            ZenIATheme {
                AppNavigation(pendingDeepLink = pendingDeepLink)
            }
        }
    }
}