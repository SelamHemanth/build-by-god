package com.buildbygod.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.components.ExerciseRow
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    onOpenExercise: (String) -> Unit,
    vm: LibraryViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Exercise Library",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Browse moves by muscle target",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQuery,
                placeholder = { Text("Search exercises") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Surface2,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Pill(
                        text = "Favorites",
                        selected = state.favoritesOnly,
                        onClick = { vm.toggleFavoritesOnly() }
                    )
                }
                item {
                    Pill(text = "All", selected = state.selectedMuscle == null, onClick = { vm.onMuscle(null) })
                }
                items(MuscleGroup.entries) { m ->
                    Pill(text = m.label, selected = state.selectedMuscle == m, onClick = { vm.onMuscle(m) })
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        if (state.grouped.isEmpty()) {
            item {
                Spacer(Modifier.height(40.dp))
                Text(
                    "No exercises match your filters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        state.grouped.forEach { (muscle, list) ->
            item(key = "h_${muscle.name}") {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        muscle.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = muscle.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${list.size}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
            items(list, key = { it.id }) { ex ->
                ExerciseRow(
                    exercise = ex,
                    onToggleFavorite = { vm.toggleFavorite(ex.id, it) },
                    onClick = { onOpenExercise(ex.id) }
                )
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}
