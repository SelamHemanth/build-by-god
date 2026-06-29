package com.buildbygod

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* weather retries on next resolve */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Coarse location lets the animated background reflect real local weather. Optional.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val initialDay = intent?.getStringExtra(NotificationConstants.EXTRA_DAY)
        val initialRoute = intent?.getStringExtra(NotificationConstants.EXTRA_ROUTE)

        setContent {
            ThemedApp(startDayDeepLink = initialDay, startRoute = initialRoute)
        }
    }
}

@Composable
private fun ThemedApp(
    startDayDeepLink: String?,
    startRoute: String?,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    BuildByGodTheme(
        themeMode = settings.themeMode,
        accentScheme = settings.accentScheme,
        glassIntensity = settings.glassIntensity
    ) {
        BuildByGodRoot(startDayDeepLink = startDayDeepLink, startRoute = startRoute)
    }
}
