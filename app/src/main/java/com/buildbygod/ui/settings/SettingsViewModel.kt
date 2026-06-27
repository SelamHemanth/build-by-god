package com.buildbygod.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.AppSettings
import com.buildbygod.data.repository.SettingsRepository
import com.buildbygod.ui.theme.AccentScheme
import com.buildbygod.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val settings = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setAccent(scheme: AccentScheme) = viewModelScope.launch { repo.setAccent(scheme) }
    fun setGlass(value: Float) = viewModelScope.launch { repo.setGlassIntensity(value) }
}
