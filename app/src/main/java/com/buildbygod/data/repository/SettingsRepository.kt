package com.buildbygod.data.repository

import com.buildbygod.data.datastore.AppSettings
import com.buildbygod.data.datastore.SettingsDataStore
import com.buildbygod.ui.theme.AccentScheme
import com.buildbygod.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val store: SettingsDataStore
) {
    val settings: Flow<AppSettings> = store.settings
    suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
    suspend fun setAccent(scheme: AccentScheme) = store.setAccent(scheme)
    suspend fun setGlassIntensity(value: Float) = store.setGlassIntensity(value)
}
