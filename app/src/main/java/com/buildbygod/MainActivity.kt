package com.buildbygod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.notifications.NotificationConstants
import com.buildbygod.ui.BuildByGodRoot
import com.buildbygod.ui.settings.SettingsViewModel
import com.buildbygod.ui.theme.BuildByGodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val initialDay = intent?.getStringExtra(NotificationConstants.EXTRA_DAY)

        setContent {
            ThemedApp(startDayDeepLink = initialDay)
        }
    }
}

@Composable
private fun ThemedApp(
    startDayDeepLink: String?,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    BuildByGodTheme(
        themeMode = settings.themeMode,
        accentScheme = settings.accentScheme,
        glassIntensity = settings.glassIntensity
    ) {
        BuildByGodRoot(startDayDeepLink = startDayDeepLink)
    }
}
