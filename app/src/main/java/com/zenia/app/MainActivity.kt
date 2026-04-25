package com.zenia.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import com.zenia.app.ui.navigation.AppNavigation
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.zenia.app.ui.components.ZeniaSnackbarHost

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            checkForAppUpdates()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdates()

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
            val windowSizeClass = calculateWindowSizeClass(this)
            ZenIATheme(windowSizeClass = windowSizeClass.widthSizeClass) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(pendingDeepLink = pendingDeepLink)

                    ZeniaSnackbarHost()
                }
            }
        }
    }

    private fun checkForAppUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }
}