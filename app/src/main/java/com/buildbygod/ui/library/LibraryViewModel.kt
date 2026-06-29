package com.buildbygod.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.repository.ExerciseRepository
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ordering options for the exercise library. */
enum class ExerciseSort(val label: String) {
    NAME_AZ("Name A–Z"),
    NAME_ZA("Name Z–A"),
    DIFFICULTY_EASY("Easiest first"),
    DIFFICULTY_HARD("Hardest first")
}

data class LibraryUiState(
    val query: String = "",
    val selectedMuscle: MuscleGroup? = null,
    val favoritesOnly: Boolean = false,
    val selectedDifficulty: Difficulty? = null,
    val selectedEquipment: Equipment? = null,
    val sort: ExerciseSort = ExerciseSort.NAME_AZ,
    val grouped: Map<MuscleGroup, List<ExerciseEntity>> = emptyMap(),
    /** When a single group is open, its exercises split by difficulty tier. */
    val byDifficulty: Map<Difficulty, List<ExerciseEntity>> = emptyMap(),
    /** Total exercises per muscle group, for the group picker grid. */
    val groupCounts: Map<MuscleGroup, Int> = emptyMap(),
    /** Number of exercises matching the active filters (for the result count label). */
    val resultCount: Int = 0
) {
    val hasActiveFilter: Boolean
        get() = query.isNotBlank() || selectedMuscle != null || favoritesOnly ||
            selectedDifficulty != null || selectedEquipment != null

    /** Show the group-icon grid only when nothing is being filtered. */
    val showGroups: Boolean get() = !hasActiveFilter

    /** Inside a single muscle group we organise by difficulty; search/favorites group by muscle. */
    val showByDifficulty: Boolean get() = selectedMuscle != null
}

private data class LibraryFilters(
    val query: String,
    val muscle: MuscleGroup?,
    val favoritesOnly: Boolean,
    val difficulty: Difficulty?,
    val equipment: Equipment?
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    private val favoritesOnly = MutableStateFlow(false)
    private val selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    private val selectedEquipment = MutableStateFlow<Equipment?>(null)
    private val sort = MutableStateFlow(ExerciseSort.NAME_AZ)

    private val filters = combine(
        query, selectedMuscle, favoritesOnly, selectedDifficulty, selectedEquipment
    ) { q, muscle, favOnly, difficulty, equipment ->
        LibraryFilters(q, muscle, favOnly, difficulty, equipment)
    }

    val state = combine(repo.all(), filters, sort) { all, f, sortOrder ->
        val comparator: Comparator<ExerciseEntity> = when (sortOrder) {
            ExerciseSort.NAME_AZ -> compareBy { it.name.lowercase() }
            ExerciseSort.NAME_ZA -> compareByDescending { it.name.lowercase() }
            ExerciseSort.DIFFICULTY_EASY -> compareBy<ExerciseEntity> { Difficulty.fromName(it.difficulty).rank }.thenBy { it.name.lowercase() }
            ExerciseSort.DIFFICULTY_HARD -> compareByDescending<ExerciseEntity> { Difficulty.fromName(it.difficulty).rank }.thenBy { it.name.lowercase() }
        }
        val filtered = all.asSequence()
            .filter { f.query.isBlank() || it.name.contains(f.query, ignoreCase = true) }
            .filter { f.muscle == null || it.muscleGroup == f.muscle.name }
            .filter { !f.favoritesOnly || it.isFavorite }
            .filter { f.difficulty == null || it.difficulty == f.difficulty.name }
            .filter { f.equipment == null || it.equipment == f.equipment.name }
            .sortedWith(comparator)
            .toList()
        val grouped = filtered.groupBy { MuscleGroup.fromName(it.muscleGroup) }
            .toSortedMap(compareBy { it.ordinal })
        val byDifficulty = filtered.groupBy { Difficulty.fromName(it.difficulty) }
            .toSortedMap(compareBy { it.rank })
        val counts = all.groupingBy { MuscleGroup.fromName(it.muscleGroup) }.eachCount()
        LibraryUiState(
            query = f.query,
            selectedMuscle = f.muscle,
            favoritesOnly = f.favoritesOnly,
            selectedDifficulty = f.difficulty,
            selectedEquipment = f.equipment,
            sort = sortOrder,
            grouped = grouped,
            byDifficulty = byDifficulty,
            groupCounts = counts,
            resultCount = filtered.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQuery(q: String) = query.update { q }
    fun onMuscle(m: MuscleGroup?) = selectedMuscle.update { if (it == m) null else m }
    fun toggleFavoritesOnly() = favoritesOnly.update { !it }
    fun onDifficulty(d: Difficulty?) = selectedDifficulty.update { if (it == d) null else d }
    fun onEquipment(e: Equipment?) = selectedEquipment.update { if (it == e) null else e }
    fun onSort(s: ExerciseSort) = sort.update { s }

    /** Reset all filters and return to the group-icon grid. */
    fun backToGroups() {
        query.update { "" }
        selectedMuscle.update { null }
        favoritesOnly.update { false }
        selectedDifficulty.update { null }
        selectedEquipment.update { null }
    }
    fun toggleFavorite(id: String, fav: Boolean) {
        viewModelScope.launch { repo.setFavorite(id, fav) }
    }
}
