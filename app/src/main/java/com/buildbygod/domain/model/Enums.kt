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

/** Training experience, framed by how long the user has been working out. */
enum class ExperienceLevel(val label: String, val duration: String, val blurb: String) {
    NEW("New", "0–3 months", "Just getting started"),
    BEGINNER("Beginner", "3–12 months", "Learning the basics"),
    INTERMEDIATE("Intermediate", "1–3 years", "Consistent and confident"),
    ADVANCED("Advanced", "3–5 years", "Strong, refined technique"),
    ELITE("Elite", "5+ years", "Highly experienced");

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: BEGINNER
    }
}

/** How the user prefers to enter/see their height. Canonical storage is always centimetres. */
enum class HeightUnit(val label: String) {
    CM("cm"),
    FT_IN("ft / in"),
    IN("inch");

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: CM
    }
}

/** How the user prefers to enter/see their weight. Canonical storage is always kilograms. */
enum class WeightUnit(val label: String, val suffix: String) {
    KG("Kilograms", "kg"),
    LB("Pounds", "lb"),
    ST("Stone", "st");

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: KG
    }
}

/** Height/weight conversions between canonical units (cm, kg) and display units. */
object UnitConvert {
    private const val CM_PER_IN = 2.54
    private const val LB_PER_KG = 2.2046226218
    private const val KG_PER_ST = 6.35029

    fun cmToInches(cm: Int): Double = cm / CM_PER_IN
    fun inchesToCm(inches: Double): Int = Math.round(inches * CM_PER_IN).toInt()
    fun cmToFeetInches(cm: Int): Pair<Int, Int> {
        val totalIn = Math.round(cm / CM_PER_IN).toInt()
        return (totalIn / 12) to (totalIn % 12)
    }
    fun feetInchesToCm(feet: Int, inches: Int): Int = inchesToCm(feet * 12.0 + inches)

    fun kgToLb(kg: Float): Double = kg * LB_PER_KG
    fun lbToKg(lb: Double): Float = (lb / LB_PER_KG).toFloat()
    fun kgToSt(kg: Float): Double = kg / KG_PER_ST
    fun stToKg(st: Double): Float = (st * KG_PER_ST).toFloat()

    /** Pretty height label for the chosen unit, from canonical cm. */
    fun formatHeight(cm: Int, unit: HeightUnit): String = when (unit) {
        HeightUnit.CM -> "$cm cm"
        HeightUnit.IN -> "${Math.round(cmToInches(cm))} in"
        HeightUnit.FT_IN -> cmToFeetInches(cm).let { (ft, inch) -> "$ft' $inch\"" }
    }

    /** Pretty weight label for the chosen unit, from canonical kg. */
    fun formatWeight(kg: Float, unit: WeightUnit): String = when (unit) {
        WeightUnit.KG -> "${trim(kg.toDouble())} kg"
        WeightUnit.LB -> "${trim(kgToLb(kg))} lb"
        WeightUnit.ST -> "${trim(kgToSt(kg))} st"
    }

    private fun trim(v: Double): String {
        val r = Math.round(v * 10.0) / 10.0
        return if (r % 1.0 == 0.0) r.toInt().toString() else r.toString()
    }
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
