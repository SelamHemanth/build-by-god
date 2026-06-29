package com.buildbygod.data.local

import android.content.Context
import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import org.json.JSONArray
import java.time.DayOfWeek

/**
 * Fully-offline exercise library + a sensible default weekly plan.
 *
 * Every exercise (data + a bundled looping demo clip) is shipped inside the app, derived from the
 * public-domain free-exercise-db (Unlicense). Nothing is fetched from YouTube or the web at runtime.
 * The library JSON lives at `assets/exercises.json` and demo clips under `assets/clips`.
 */
object SeedData {

    /** Parse the bundled exercise catalogue from assets. */
    fun loadExercises(context: Context): List<ExerciseEntity> {
        val raw = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        val list = ArrayList<ExerciseEntity>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val clip = o.optString("clipAsset", "").ifBlank { null }
            list += ExerciseEntity(
                id = o.getString("id"),
                name = o.getString("name"),
                type = o.optString("type", "MAIN"),
                muscleGroup = o.optString("muscleGroup", "ABS"),
                difficulty = o.optString("difficulty", "BEGINNER"),
                equipment = o.optString("equipment", "BODYWEIGHT"),
                instructions = o.optString("instructions", ""),
                tips = o.optString("tips", ""),
                defaultSets = o.optInt("defaultSets", 3),
                defaultReps = o.optString("defaultReps", "8-12"),
                durationSeconds = o.optInt("durationSeconds", 0),
                clipAsset = clip,
                youtubeUrl = null
            )
        }
        return list
    }

    val defaultDays: List<WorkoutDayEntity> by lazy {
        listOf(
            WorkoutDayEntity(DayOfWeek.MONDAY.value, "Push Day", "Chest, Shoulders & Triceps", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.TUESDAY.value, "Pull Day", "Back, Lats & Biceps", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.WEDNESDAY.value, "Leg Day", "Quads, Hamstrings & Calves", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.THURSDAY.value, "Core & Cardio", "Abs, Obliques & Conditioning", 7 * 60, true),
            WorkoutDayEntity(DayOfWeek.FRIDAY.value, "Upper Body", "Chest, Back & Arms", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.SATURDAY.value, "Glutes & Legs", "Glutes & Lower Body", 10 * 60, true),
            WorkoutDayEntity(DayOfWeek.SUNDAY.value, "Rest & Recover", "Mobility & Stretching", -1, false, isRestDay = true),
        )
    }

    /**
     * Builds a balanced weekly plan from whatever exercises were seeded, picking by muscle group and
     * section. Bodyweight/barbell staples are preferred so the default plan needs minimal equipment.
     */
    fun buildPlan(exercises: List<ExerciseEntity>): List<DayExerciseCrossRef> {
        fun pool(type: ExerciseType, vararg groups: MuscleGroup): List<ExerciseEntity> {
            val names = groups.map { it.name }.toSet()
            return exercises
                .filter { it.type == type.name && (names.isEmpty() || it.muscleGroup in names) }
                .sortedWith(compareBy({ equipmentRank(it.equipment) }, { it.name }))
        }

        val warmups = pool(ExerciseType.WARMUP)
        val stretches = pool(ExerciseType.STRETCH)

        data class Slot(val type: ExerciseType, val group: MuscleGroup?, val count: Int)

        fun day(day: DayOfWeek, slots: List<Slot>): List<DayExerciseCrossRef> {
            val refs = mutableListOf<DayExerciseCrossRef>()
            val counters = mutableMapOf<ExerciseType, Int>()
            val used = mutableSetOf<String>()
            slots.forEach { slot ->
                val candidates = when (slot.type) {
                    ExerciseType.WARMUP -> warmups
                    ExerciseType.STRETCH -> stretches
                    ExerciseType.MAIN -> pool(ExerciseType.MAIN, *(slot.group?.let { arrayOf(it) } ?: arrayOf()))
                }
                candidates.asSequence().filter { it.id !in used }.take(slot.count).forEach { ex ->
                    used += ex.id
                    val order = counters.getOrDefault(slot.type, 0)
                    counters[slot.type] = order + 1
                    refs += DayExerciseCrossRef(
                        dayOfWeek = day.value,
                        exerciseId = ex.id,
                        section = slot.type.name,
                        orderIndex = order,
                        sets = ex.defaultSets,
                        reps = ex.defaultReps
                    )
                }
            }
            return refs
        }

        val plan = mutableListOf<DayExerciseCrossRef>()
        plan += day(DayOfWeek.MONDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.CHEST, 3),
            Slot(ExerciseType.MAIN, MuscleGroup.SHOULDERS, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.TRICEPS, 2),
            Slot(ExerciseType.STRETCH, null, 2),
        ))
        plan += day(DayOfWeek.TUESDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.LATS, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.UPPER_BACK, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.BICEPS, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.FOREARMS, 1),
            Slot(ExerciseType.STRETCH, null, 1),
        ))
        plan += day(DayOfWeek.WEDNESDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.QUADS, 3),
            Slot(ExerciseType.MAIN, MuscleGroup.HAMSTRINGS, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.CALVES, 1),
            Slot(ExerciseType.STRETCH, null, 2),
        ))
        plan += day(DayOfWeek.THURSDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.ABS, 3),
            Slot(ExerciseType.MAIN, MuscleGroup.OBLIQUES, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.HIP_FLEXORS, 1),
            Slot(ExerciseType.STRETCH, null, 1),
        ))
        plan += day(DayOfWeek.FRIDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.CHEST, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.LATS, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.SHOULDERS, 1),
            Slot(ExerciseType.MAIN, MuscleGroup.BICEPS, 1),
            Slot(ExerciseType.MAIN, MuscleGroup.TRICEPS, 1),
            Slot(ExerciseType.STRETCH, null, 1),
        ))
        plan += day(DayOfWeek.SATURDAY, listOf(
            Slot(ExerciseType.WARMUP, null, 2),
            Slot(ExerciseType.MAIN, MuscleGroup.GLUTES, 3),
            Slot(ExerciseType.MAIN, MuscleGroup.QUADS, 1),
            Slot(ExerciseType.MAIN, MuscleGroup.HAMSTRINGS, 1),
            Slot(ExerciseType.MAIN, MuscleGroup.CALVES, 1),
            Slot(ExerciseType.STRETCH, null, 1),
        ))
        plan += day(DayOfWeek.SUNDAY, listOf(
            Slot(ExerciseType.STRETCH, null, 5),
        ))
        return plan
    }

    private fun equipmentRank(equipment: String): Int = when (equipment) {
        "BODYWEIGHT" -> 0
        "BARBELL" -> 1
        "DUMBBELL" -> 2
        "CABLE" -> 3
        "MACHINE" -> 4
        else -> 5
    }
}
