package com.buildbygod.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.weather.WeatherRepository
import com.buildbygod.domain.model.WeatherInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repo: WeatherRepository
) : ViewModel() {

    private val _weather = MutableStateFlow(
        // Start with a season-only guess so the backdrop is never empty while we resolve live weather.
        WeatherInfo(season = WeatherInfo.seasonFor(LocalDate.now(), null))
    )
    val weather = _weather.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _weather.value = repo.current()
        }
    }
}
