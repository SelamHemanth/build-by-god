package com.buildbygod.data.local

import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.domain.model.Difficulty
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.MuscleGroup

/**
 * Turns a free-text day name ("Leg Day", "Push", "Arms & Abs") into a concrete workout:
 * warm-ups, main exercises for the implied muscle groups, and stretches — tailored to the
 * user's experience (difficulty) and goal (sets/reps).
 */
object DayPlanner {

    data class Generated(
        val title: String,
        val focus: String,
        val refs: List<DayExerciseCrossRef>
    )

    /** Ordered keyword -> muscle groups. First match wins per keyword; order shapes the focus label. */
    private val keywordGroups: List<Pair<String, List<MuscleGroup>>> = listOf(
        "full body" to MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY },
        "upper" to listOf(MuscleGroup.CHEST, MuscleGroup.UPPER_BACK, MuscleGroup.SHOULDERS, MuscleGroup.BICEPS, MuscleGroup.TRICEPS),
        "lower" to listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES),
        "push" to listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
        "pull" to listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK, MuscleGroup.BICEPS),
        "leg" to listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES),
        "quad" to listOf(MuscleGroup.QUADS),
        "hamstring" to listOf(MuscleGroup.HAMSTRINGS),
        "glute" to listOf(MuscleGroup.GLUTES),
        "calf" to listOf(MuscleGroup.CALVES),
        "calve" to listOf(MuscleGroup.CALVES),
        "core" to listOf(MuscleGroup.ABS, MuscleGroup.OBLIQUES),
        "oblique" to listOf(MuscleGroup.OBLIQUES),
        "ab" to listOf(MuscleGroup.ABS, MuscleGroup.OBLIQUES),
        "chest" to listOf(MuscleGroup.CHEST),
        "lat" to listOf(MuscleGroup.LATS),
        "back" to listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK, MuscleGroup.LOWER_BACK),
        "shoulder" to listOf(MuscleGroup.SHOULDERS),
        "delt" to listOf(MuscleGroup.SHOULDERS),
        "trap" to listOf(MuscleGroup.TRAPS),
        "bicep" to listOf(MuscleGroup.BICEPS),
        "tricep" to listOf(MuscleGroup.TRICEPS),
        "forearm" to listOf(MuscleGroup.FOREARMS),
        "arm" to listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS),
        "neck" to listOf(MuscleGroup.NECK),
        "chest" to listOf(MuscleGroup.CHEST)
    )

    fun parseGroups(name: String): List<MuscleGroup> {
        val n = name.lowercase()
        val result = LinkedHashSet<MuscleGroup>()
        keywordGroups.forEach { (kw, groups) ->
            if (n.contains(kw)) result.addAll(groups)
        }
        return result.toList()
    }

    private fun targetDifficulty(exp: ExperienceLevel): Difficulty = when (exp) {
        ExperienceLevel.NEW, ExperienceLevel.BEGINNER -> Difficulty.BEGINNER
        ExperienceLevel.INTERMEDIATE -> Difficulty.INTERMEDIATE
        ExperienceLevel.ADVANCED, ExperienceLevel.ELITE -> Difficulty.ADVANCED
    }

    /** Number of main exercises by experience. */
    private fun mainCount(exp: ExperienceLevel): Int = when (exp) {
        ExperienceLevel.NEW -> 4
        ExperienceLevel.BEGINNER -> 5
        ExperienceLevel.INTERMEDIATE -> 6
        ExperienceLevel.ADVANCED -> 7
        ExperienceLevel.ELITE -> 8
    }

    private fun setsReps(goal: Goal): Pair<Int, String> = when (goal) {
        Goal.BUILD_MUSCLE -> 4 to "8-12"
        Goal.GET_STRONGER -> 5 to "3-5"
        Goal.LOSE_FAT -> 3 to "12-15"
        Goal.IMPROVE_MOBILITY -> 2 to "10-12"
        Goal.STAY_FIT -> 3 to "10-12"
    }

    private fun equipmentRank(equipment: String): Int = when (equipment) {
        "BODYWEIGHT" -> 0
        "BARBELL" -> 1
        "DUMBBELL" -> 2
        "CABLE" -> 3
        "MACHINE" -> 4
        else -> 5
    }

    fun build(
        name: String,
        exercises: List<ExerciseEntity>,
        experience: ExperienceLevel,
        goal: Goal
    ): Generated {
        val cleanName = name.trim().ifBlank { "Workout" }
        val groups = parseGroups(cleanName).ifEmpty {
            listOf(MuscleGroup.CHEST, MuscleGroup.UPPER_BACK, MuscleGroup.QUADS, MuscleGroup.ABS)
        }
        val target = targetDifficulty(experience)

        fun pick(pool: List<ExerciseEntity>, count: Int, used: MutableSet<String>): List<ExerciseEntity> {
            // Prefer exercises at the target difficulty, then fall back to neighbours.
            val sorted = pool.sortedWith(
                compareBy(
                    { kotlin.math.abs(Difficulty.fromName(it.difficulty).rank - target.rank) },
                    { equipmentRank(it.equipment) },
                    { it.name }
                )
            )
            return sorted.asSequence().filter { it.id !in used }.take(count).onEach { used += it.id }.toList()
        }

        val used = mutableSetOf<String>()
        val refs = mutableListOf<DayExerciseCrossRef>()
        val (sets, reps) = setsReps(goal)

        // Warm-ups (2)
        val warmups = pick(exercises.filter { it.type == ExerciseType.WARMUP.name }, 2, used)
        warmups.forEachIndexed { i, ex ->
            refs += DayExerciseCrossRef(dayOfWeek = 0, exerciseId = ex.id, section = ExerciseType.WARMUP.name, orderIndex = i, sets = ex.defaultSets, reps = ex.defaultReps)
        }

        // Main exercises distributed across the implied groups.
        val totalMains = mainCount(experience)
        val perGroup = maxOf(1, totalMains / groups.size)
        var mainOrder = 0
        groups.forEach { g ->
            val pool = exercises.filter { it.type == ExerciseType.MAIN.name && it.muscleGroup == g.name }
            pick(pool, perGroup, used).forEach { ex ->
                refs += DayExerciseCrossRef(dayOfWeek = 0, exerciseId = ex.id, section = ExerciseType.MAIN.name, orderIndex = mainOrder++, sets = sets, reps = reps)
            }
        }
        // Top up if rounding left us short.
        if (mainOrder < totalMains) {
            val extraPool = exercises.filter { ex ->
                ex.type == ExerciseType.MAIN.name && groups.any { it.name == ex.muscleGroup }
            }
            pick(extraPool, totalMains - mainOrder, used).forEach { ex ->
                refs += DayExerciseCrossRef(dayOfWeek = 0, exerciseId = ex.id, section = ExerciseType.MAIN.name, orderIndex = mainOrder++, sets = sets, reps = reps)
            }
        }

        // Stretches (2)
        val stretches = pick(exercises.filter { it.type == ExerciseType.STRETCH.name }, 2, used)
        stretches.forEachIndexed { i, ex ->
            refs += DayExerciseCrossRef(dayOfWeek = 0, exerciseId = ex.id, section = ExerciseType.STRETCH.name, orderIndex = i, sets = ex.defaultSets, reps = ex.defaultReps)
        }

        val focus = groups.take(3).joinToString(", ") { it.label }
        return Generated(cleanName, focus, refs)
    }
}
