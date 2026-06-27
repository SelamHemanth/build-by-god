package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.buildbygod.ui.theme.AccentScheme
import com.buildbygod.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentScheme: AccentScheme = AccentScheme.AURORA,
    val glassIntensity: Float = 0.65f
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_scheme")
        val GLASS = floatPreferencesKey("glass_intensity")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { p ->
        AppSettings(
            themeMode = ThemeMode.fromName(p[Keys.THEME]),
            accentScheme = AccentScheme.fromName(p[Keys.ACCENT]),
            glassIntensity = (p[Keys.GLASS] ?: 0.65f).coerceIn(0f, 1f)
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setAccent(scheme: AccentScheme) {
        context.settingsDataStore.edit { it[Keys.ACCENT] = scheme.name }
    }

    suspend fun setGlassIntensity(value: Float) {
        context.settingsDataStore.edit { it[Keys.GLASS] = value.coerceIn(0f, 1f) }
    }
}
