package com.buildbygod.domain.model

import androidx.compose.ui.graphics.Color
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.AccentViolet

/** The three sections every workout day is split into. */
enum class ExerciseType(val label: String) {
    WARMUP("Warm-up"),
    MAIN("Exercise"),
    STRETCH("Stretch")
}

enum class MuscleGroup(val label: String, val accent: Color) {
    ABS("Abs", AccentAmber),
    OBLIQUES("Obliques", AccentBlue),
    CHEST("Chest", AccentPink),
    SHOULDERS("Shoulders", AccentAmber),
    TRAPS("Traps", AccentViolet),
    BICEPS("Biceps", AccentGreen),
    TRICEPS("Triceps", AccentPink),
    FOREARMS("Forearms", AccentBlue),
    PALMAR_FASCIA("Palmar Fascia", AccentAmber),
    LATS("Lats", AccentViolet),
    UPPER_BACK("Upper Back", AccentBlue),
    LOWER_BACK("Lower Back", AccentGreen),
    NECK("Neck", AccentPink),
    GLUTES("Glutes", AccentPink),
    HIP_FLEXORS("Hip Flexors", AccentViolet),
    ADDUCTORS("Adductors", AccentGreen),
    ABDUCTORS("Abductors", AccentBlue),
    QUADS("Quads", AccentGreen),
    HAMSTRINGS("Hamstrings", AccentAmber),
    CALVES("Calves", AccentViolet),
    IT_BAND("IT Band", AccentPink),
    PLANTAR_FASCIA("Plantar Fascia", AccentGreen),
    FULL_BODY("Full Body", AccentViolet);

    companion object {
        fun fromName(name: String?): MuscleGroup =
            entries.firstOrNull { it.name == name } ?: ABS
    }
}

enum class Equipment(val label: String) {
    BODYWEIGHT("Bodyweight"),
    DUMBBELL("Dumbbell"),
    BARBELL("Barbell"),
    MACHINE("Machine"),
    CABLE("Cable"),
    KETTLEBELL("Kettlebell"),
    BAND("Resistance Band"),
    NONE("No Equipment");

    companion object {
        fun fromName(name: String?): Equipment =
            entries.firstOrNull { it.name == name } ?: BODYWEIGHT
    }
}

enum class Units(val label: String, val weightSuffix: String) {
    METRIC("Metric (kg)", "kg"),
    IMPERIAL("Imperial (lb)", "lb")
}

enum class Goal(val label: String) {
    BUILD_MUSCLE("Build Muscle"),
    LOSE_FAT("Lose Fat"),
    GET_STRONGER("Get Stronger"),
    STAY_FIT("Stay Fit"),
    IMPROVE_MOBILITY("Improve Mobility")
}

enum class Sex(val label: String) {
    MALE("Male"),
    FEMALE("Female");

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: MALE
    }
}

enum class ActivityLevel(val label: String, val factor: Double) {
    SEDENTARY("Sedentary", 1.2),
    LIGHT("Lightly active", 1.375),
    MODERATE("Moderately active", 1.55),
    ACTIVE("Very active", 1.725),
    ATHLETE("Athlete", 1.9);

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: MODERATE
    }
}
