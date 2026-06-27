package com.buildbygod.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.repository.ExerciseRepository
import com.buildbygod.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val query: String = "",
    val selectedMuscle: MuscleGroup? = null,
    val favoritesOnly: Boolean = false,
    val grouped: Map<MuscleGroup, List<ExerciseEntity>> = emptyMap()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    private val favoritesOnly = MutableStateFlow(false)

    val state = combine(
        repo.all(),
        query,
        selectedMuscle,
        favoritesOnly
    ) { all, q, muscle, favOnly ->
        val filtered = all.asSequence()
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
            .filter { muscle == null || it.muscleGroup == muscle.name }
            .filter { !favOnly || it.isFavorite }
            .toList()
        val grouped = filtered.groupBy { MuscleGroup.fromName(it.muscleGroup) }
            .toSortedMap(compareBy { it.ordinal })
        LibraryUiState(q, muscle, favOnly, grouped)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQuery(q: String) = query.update { q }
    fun onMuscle(m: MuscleGroup?) = selectedMuscle.update { if (it == m) null else m }
    fun toggleFavoritesOnly() = favoritesOnly.update { !it }
    fun toggleFavorite(id: String, fav: Boolean) {
        viewModelScope.launch { repo.setFavorite(id, fav) }
    }
}
