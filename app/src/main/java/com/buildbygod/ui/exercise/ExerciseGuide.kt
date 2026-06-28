package com.buildbygod.ui.exercise

import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup

/**
 * Derives beginner-friendly overview text, readable tags and coaching tips from the raw
 * exercise fields. The bundled catalog stores its metadata (level / force / mechanic) as a
 * comma list in the `tips` column, so we parse that here instead of inventing new data.
 */
object ExerciseGuide {

    private val levels = setOf("beginner", "intermediate", "expert", "advanced")
    private val forces = setOf("push", "pull", "static")
    private val mechanics = setOf("compound", "isolation")

    data class Meta(
        val level: String?,
        val force: String?,
        val mechanic: String?,
        val tags: List<String>
    )

    fun parse(raw: String): Meta {
        val parts = raw.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val level = parts.firstOrNull { it in levels }
        val force = parts.firstOrNull { it in forces }
        val mechanic = parts.firstOrNull { it in mechanics }
        val tags = buildList {
            level?.let { add(it.replaceFirstChar { c -> c.uppercase() }) }
            mechanic?.let { add(it.replaceFirstChar { c -> c.uppercase() }) }
            force?.let { add(if (it == "static") "Hold" else it.replaceFirstChar { c -> c.uppercase() } + " move") }
        }
        return Meta(level, force, mechanic, tags)
    }

    fun overview(
        name: String,
        muscle: MuscleGroup,
        equipment: Equipment,
        type: ExerciseType,
        meta: Meta
    ): String {
        val mechanicWord = when (meta.mechanic) {
            "compound" -> "compound (multi-joint)"
            "isolation" -> "isolation (single-joint)"
            else -> ""
        }
        val typeWord = when (type) {
            ExerciseType.WARMUP -> "warm-up movement"
            ExerciseType.STRETCH -> "stretch"
            else -> "exercise"
        }
        val equip = when (equipment) {
            Equipment.BODYWEIGHT -> "It needs no equipment — just your bodyweight"
            else -> "It uses ${equipment.label.lowercase()}"
        }
        val builder = StringBuilder()
        builder.append("The $name is a ")
        if (mechanicWord.isNotEmpty()) builder.append("$mechanicWord ")
        builder.append("$typeWord that mainly works your ${muscle.label.lowercase()}. ")
        builder.append("$equip. ")
        builder.append(
            when (type) {
                ExerciseType.WARMUP -> "Use it to raise your heart rate and prime the muscle before your main sets."
                ExerciseType.STRETCH -> "Use it to improve flexibility and help the muscle recover."
                else -> if (meta.level == "beginner")
                    "It's beginner friendly, so focus on clean technique before adding load."
                else "Keep the movement controlled and aim for a full range of motion on every rep."
            }
        )
        return builder.toString()
    }

    fun tips(muscle: MuscleGroup, type: ExerciseType, meta: Meta): List<String> {
        val tips = mutableListOf<String>()
        when (type) {
            ExerciseType.STRETCH -> {
                tips += "Ease into the stretch slowly — never bounce or force it."
                tips += "Hold steadily and breathe deeply; you should feel tension, not pain."
                tips += "Relax a little deeper on each exhale."
            }
            ExerciseType.WARMUP -> {
                tips += "Start gently and build up the pace over the first few reps."
                tips += "Keep moving smoothly — the goal is to feel warm, not tired."
            }
            else -> {
                tips += "Move with control: about 2 seconds to lift and 2 seconds to lower."
                tips += "Breathe out as you exert effort, breathe in as you return."
                tips += "Keep your ${muscle.label.lowercase()} working through the whole range of motion."
                if (meta.mechanic == "compound") {
                    tips += "Brace your core and keep a neutral, tall spine throughout."
                }
                if (meta.level == "beginner") {
                    tips += "Start light and only add weight once your form feels solid."
                } else {
                    tips += "Stop 1–2 reps before failure to keep each rep clean."
                }
            }
        }
        return tips
    }
}
