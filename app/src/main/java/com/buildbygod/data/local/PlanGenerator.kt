package com.buildbygod.data.local

import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.MuscleGroup
import java.time.DayOfWeek

/**
 * Builds a personalized weekly plan from the user's goals + training experience.
 *
 * Experience decides how many days per week and how much volume; goals tweak emphasis
 * (extra core for fat loss, extra mobility work for flexibility, heavier compounds for strength).
 * Everything is picked from the bundled offline catalog, so no network is involved.
 */
object PlanGenerator {

    data class DayPlan(
        val day: DayOfWeek,
        val title: String,
        val focus: String,
        val muscles: List<MuscleGroup>,
        val warmups: Int,
        val stretches: Int,
        val perMuscle: Int,
        val maxMain: Int,
        val includeCore: Boolean,
        val isRest: Boolean
    )

    private val PUSH = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    private val PULL = listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    private val LEGS = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES)
    private val UPPER = listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.SHOULDERS, MuscleGroup.BICEPS, MuscleGroup.TRICEPS)

    private fun trainingDays(exp: ExperienceLevel) = when (exp) {
        ExperienceLevel.NEW -> 3
        ExperienceLevel.BEGINNER -> 4
        ExperienceLevel.INTERMEDIATE -> 5
        ExperienceLevel.ADVANCED -> 6
        ExperienceLevel.ELITE -> 6
    }

    private fun perMuscle(exp: ExperienceLevel) = when (exp) {
        ExperienceLevel.NEW -> 1
        ExperienceLevel.BEGINNER, ExperienceLevel.INTERMEDIATE -> 2
        ExperienceLevel.ADVANCED, ExperienceLevel.ELITE -> 3
    }

    private fun maxMain(exp: ExperienceLevel) = when (exp) {
        ExperienceLevel.NEW -> 4
        ExperienceLevel.BEGINNER -> 5
        ExperienceLevel.INTERMEDIATE -> 6
        ExperienceLevel.ADVANCED -> 8
        ExperienceLevel.ELITE -> 9
    }

    private fun warmups(exp: ExperienceLevel) =
        if (exp == ExperienceLevel.NEW || exp == ExperienceLevel.BEGINNER) 1 else 2

    private data class Template(val title: String, val focus: String, val muscles: List<MuscleGroup>)

    private fun templatesFor(n: Int): List<Template> = when (n) {
        3 -> listOf(
            Template("Full Body A", "Chest, Back & Legs", listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.QUADS, MuscleGroup.ABS)),
            Template("Full Body B", "Shoulders, Back & Hamstrings", listOf(MuscleGroup.SHOULDERS, MuscleGroup.UPPER_BACK, MuscleGroup.HAMSTRINGS, MuscleGroup.OBLIQUES)),
            Template("Full Body C", "Arms, Glutes & Calves", listOf(MuscleGroup.CHEST, MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.GLUTES, MuscleGroup.CALVES))
        )
        4 -> listOf(
            Template("Upper A", "Chest, Back & Arms", UPPER),
            Template("Lower A", "Quads, Hamstrings & Glutes", LEGS),
            Template("Upper B", "Shoulders, Back & Arms", listOf(MuscleGroup.SHOULDERS, MuscleGroup.UPPER_BACK, MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.BICEPS)),
            Template("Lower B", "Glutes, Hamstrings & Calves", listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS, MuscleGroup.CALVES))
        )
        5 -> listOf(
            Template("Push Day", "Chest, Shoulders & Triceps", PUSH),
            Template("Pull Day", "Back, Lats & Biceps", PULL),
            Template("Leg Day", "Quads, Hamstrings & Calves", LEGS),
            Template("Upper Body", "Chest, Back & Arms", UPPER),
            Template("Lower Body", "Glutes & Legs", listOf(MuscleGroup.GLUTES, MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES))
        )
        else -> listOf(
            Template("Push A", "Chest, Shoulders & Triceps", PUSH),
            Template("Pull A", "Back, Lats & Biceps", PULL),
            Template("Legs A", "Quads, Hamstrings & Calves", LEGS),
            Template("Push B", "Shoulders, Chest & Triceps", listOf(MuscleGroup.SHOULDERS, MuscleGroup.CHEST, MuscleGroup.TRICEPS)),
            Template("Pull B", "Lats, Upper Back & Biceps", PULL),
            Template("Legs B", "Glutes, Hamstrings & Calves", listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.QUADS, MuscleGroup.CALVES))
        )
    }

    private fun scheduleFor(n: Int): List<DayOfWeek> = when (n) {
        3 -> listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        4 -> listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        5 -> listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        else -> listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    }

    /** Pure (no DB / context) — used both for the preview and the actual build. */
    fun build(goals: Set<Goal>, experience: ExperienceLevel): List<DayPlan> {
        val n = trainingDays(experience)
        val templates = templatesFor(n)
        val slots = scheduleFor(n)
        val perMuscle = perMuscle(experience)
        val maxMain = maxMain(experience)
        val warmups = warmups(experience)
        val includeCore = Goal.LOSE_FAT in goals
        val stretchBonus = when {
            Goal.IMPROVE_MOBILITY in goals -> 2
            Goal.LOSE_FAT in goals -> 1
            else -> 0
        }

        return DayOfWeek.entries.map { dow ->
            val idx = slots.indexOf(dow)
            when {
                idx >= 0 -> {
                    val t = templates[idx]
                    DayPlan(dow, t.title, t.focus, t.muscles, warmups, 1 + stretchBonus, perMuscle, maxMain, includeCore, false)
                }
                dow == DayOfWeek.SUNDAY ->
                    DayPlan(dow, "Rest & Recover", "Mobility & Stretching", emptyList(), 0, 5, 0, 0, false, true)
                else ->
                    DayPlan(dow, "Rest Day", "Recovery & sleep", emptyList(), 0, 0, 0, 0, false, true)
            }
        }
    }

    /** Turns a [build] result into database rows by picking concrete exercises from the catalog. */
    fun buildRows(
        plan: List<DayPlan>,
        exercises: List<ExerciseEntity>,
        goals: Set<Goal>
    ): Pair<List<WorkoutDayEntity>, List<DayExerciseCrossRef>> {
        val strength = Goal.GET_STRONGER in goals
        fun pool(type: ExerciseType, group: MuscleGroup?): List<ExerciseEntity> =
            exercises
                .filter { it.type == type.name && (group == null || it.muscleGroup == group.name) }
                .sortedWith(compareBy({ equipmentRank(it.equipment, strength) }, { it.name }))

        val warmupPool = pool(ExerciseType.WARMUP, null)
        val stretchPool = pool(ExerciseType.STRETCH, null)

        val days = mutableListOf<WorkoutDayEntity>()
        val refs = mutableListOf<DayExerciseCrossRef>()

        plan.forEach { dp ->
            days += WorkoutDayEntity(
                dayOfWeek = dp.day.value,
                title = dp.title,
                focus = dp.focus,
                scheduledMinutes = if (dp.isRest) -1 else 18 * 60,
                reminderEnabled = !dp.isRest,
                isRestDay = dp.isRest
            )

            val used = mutableSetOf<String>()
            val counters = mutableMapOf<String, Int>()
            fun emit(type: ExerciseType, picks: List<ExerciseEntity>) {
                picks.forEach { ex ->
                    val order = counters.getOrDefault(type.name, 0)
                    counters[type.name] = order + 1
                    used += ex.id
                    refs += DayExerciseCrossRef(
                        dayOfWeek = dp.day.value,
                        exerciseId = ex.id,
                        section = type.name,
                        orderIndex = order,
                        sets = ex.defaultSets,
                        reps = ex.defaultReps
                    )
                }
            }

            // warm-ups
            emit(ExerciseType.WARMUP, warmupPool.filter { it.id !in used }.take(dp.warmups))

            // main lifts: round-robin across the day's muscles, capped at maxMain
            val muscles = dp.muscles + if (dp.includeCore) listOf(MuscleGroup.ABS, MuscleGroup.OBLIQUES) else emptyList()
            if (muscles.isNotEmpty()) {
                val pools = muscles.associateWith { pool(ExerciseType.MAIN, it).toMutableList() }
                val main = mutableListOf<ExerciseEntity>()
                var round = 0
                while (round < dp.perMuscle && main.size < dp.maxMain) {
                    for (m in muscles) {
                        if (main.size >= dp.maxMain) break
                        val next = pools[m]?.firstOrNull { it.id !in used && main.none { p -> p.id == it.id } }
                        if (next != null) { main += next; used += next.id }
                    }
                    round++
                }
                emit(ExerciseType.MAIN, main)
            }

            // stretches / cooldown
            emit(ExerciseType.STRETCH, stretchPool.filter { it.id !in used }.take(dp.stretches))
        }

        return days to refs
    }

    private fun equipmentRank(equipment: String, strength: Boolean): Int =
        if (strength) when (equipment) {
            "BARBELL" -> 0
            "DUMBBELL" -> 1
            "BODYWEIGHT" -> 2
            "CABLE" -> 3
            "MACHINE" -> 4
            else -> 5
        } else when (equipment) {
            "BODYWEIGHT" -> 0
            "BARBELL" -> 1
            "DUMBBELL" -> 2
            "CABLE" -> 3
            "MACHINE" -> 4
            else -> 5
        }
}
