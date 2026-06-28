package com.buildbygod.data.local

import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.domain.model.Equipment
import com.buildbygod.domain.model.ExerciseType
import com.buildbygod.domain.model.MuscleGroup
import java.time.DayOfWeek

/**
 * Curated, offline exercise library + a sensible default weekly plan.
 * Demo clips are referenced by [ExerciseEntity.clipAsset] (drop files in assets/clips and set the
 * name to enable them); each exercise also has a YouTube search link for the full demo video.
 */
object SeedData {

    private fun yt(query: String) =
        "https://www.youtube.com/results?search_query=" + query.replace(" ", "+") + "+how+to"

    /**
     * Bundled looping demo clips (animated GIFs in assets/clips), keyed by exercise id.
     * Frames are derived from the public-domain free-exercise-db (Unlicense).
     */
    private val clipById: Map<String, String> = mapOf(
        "ch_bench_press" to "clips/ch_bench_press.gif",
        "ch_incline_db_press" to "clips/ch_incline_db_press.gif",
        "ch_pushup" to "clips/ch_pushup.gif",
        "ch_cable_fly" to "clips/ch_cable_fly.gif",
        "bk_pullup" to "clips/bk_pullup.gif",
        "bk_bent_row" to "clips/bk_bent_row.gif",
        "bk_lat_pulldown" to "clips/bk_lat_pulldown.gif",
        "bk_seated_row" to "clips/bk_seated_row.gif",
        "lg_back_squat" to "clips/lg_back_squat.gif",
        "lg_rdl" to "clips/lg_rdl.gif",
        "lg_lunge" to "clips/lg_lunge.gif",
        "lg_leg_press" to "clips/lg_leg_press.gif",
        "lg_calf_raise" to "clips/lg_calf_raise.gif",
        "sh_ohp" to "clips/sh_ohp.gif",
        "sh_lateral_raise" to "clips/sh_lateral_raise.gif",
        "sh_face_pull" to "clips/sh_face_pull.gif",
        "ar_db_curl" to "clips/ar_db_curl.gif",
        "ar_tricep_pushdown" to "clips/ar_tricep_pushdown.gif",
        "ar_hammer_curl" to "clips/ar_hammer_curl.gif",
        "co_plank" to "clips/co_plank.gif",
        "co_hanging_raise" to "clips/co_hanging_raise.gif",
        "co_russian_twist" to "clips/co_russian_twist.gif",
        "gl_hip_thrust" to "clips/gl_hip_thrust.gif",
        "gl_glute_bridge" to "clips/gl_glute_bridge.gif",
        "wu_jumping_jacks" to "clips/wu_jumping_jacks.gif",
        "wu_bodyweight_squat" to "clips/wu_bodyweight_squat.gif",
    )

    private fun ex(
        id: String,
        name: String,
        type: ExerciseType,
        muscle: MuscleGroup,
        equipment: Equipment,
        steps: List<String>,
        tips: String,
        sets: Int = 3,
        reps: String = "10-12",
        duration: Int = 0
    ) = ExerciseEntity(
        id = id,
        name = name,
        type = type.name,
        muscleGroup = muscle.name,
        equipment = equipment.name,
        instructions = steps.joinToString("\n"),
        tips = tips,
        defaultSets = sets,
        defaultReps = reps,
        durationSeconds = duration,
        clipAsset = clipById[id],
        youtubeUrl = yt(name)
    )

    val exercises: List<ExerciseEntity> by lazy {
        listOf(
            // ---------------- WARM-UPS (timed) ----------------
            ex("wu_jumping_jacks", "Jumping Jacks", ExerciseType.WARMUP, MuscleGroup.CARDIO, Equipment.BODYWEIGHT,
                listOf("Stand tall with feet together and arms at your sides.",
                    "Jump while spreading your legs and raising your arms overhead.",
                    "Jump back to the start and repeat at a steady rhythm."),
                "Keep a soft bend in the knees and land lightly.", duration = 60),
            ex("wu_arm_circles", "Arm Circles", ExerciseType.WARMUP, MuscleGroup.SHOULDERS, Equipment.BODYWEIGHT,
                listOf("Extend both arms out to the sides at shoulder height.",
                    "Make small forward circles, gradually getting larger.",
                    "Reverse direction halfway through."),
                "Relax the shoulders; control the motion.", duration = 45),
            ex("wu_high_knees", "High Knees", ExerciseType.WARMUP, MuscleGroup.CARDIO, Equipment.BODYWEIGHT,
                listOf("Stand tall and run in place.",
                    "Drive each knee up toward hip height.",
                    "Pump the arms in rhythm."),
                "Stay on the balls of your feet and keep the core tight.", duration = 45),
            ex("wu_bodyweight_squat", "Bodyweight Squat", ExerciseType.WARMUP, MuscleGroup.LEGS, Equipment.BODYWEIGHT,
                listOf("Stand with feet shoulder-width apart.",
                    "Sit your hips back and down until thighs are parallel.",
                    "Drive through the heels to stand."),
                "Keep your chest up and knees tracking over toes.", duration = 60),
            ex("wu_cat_cow", "Cat-Cow", ExerciseType.WARMUP, MuscleGroup.MOBILITY, Equipment.BODYWEIGHT,
                listOf("Start on all fours, wrists under shoulders.",
                    "Inhale, drop the belly and lift the chest (cow).",
                    "Exhale, round the spine toward the ceiling (cat)."),
                "Move slowly with your breath.", duration = 45),
            ex("wu_leg_swings", "Leg Swings", ExerciseType.WARMUP, MuscleGroup.MOBILITY, Equipment.BODYWEIGHT,
                listOf("Hold a wall for balance.",
                    "Swing one leg forward and back in a controlled arc.",
                    "Switch legs after the set."),
                "Only swing as far as feels comfortable.", duration = 40),

            // ---------------- CHEST ----------------
            ex("ch_bench_press", "Barbell Bench Press", ExerciseType.MAIN, MuscleGroup.CHEST, Equipment.BARBELL,
                listOf("Lie on the bench with eyes under the bar.",
                    "Grip slightly wider than shoulders and unrack.",
                    "Lower the bar to mid-chest with control.",
                    "Press up until arms are extended."),
                "Keep shoulder blades retracted and feet planted.", sets = 4, reps = "6-10"),
            ex("ch_incline_db_press", "Incline Dumbbell Press", ExerciseType.MAIN, MuscleGroup.CHEST, Equipment.DUMBBELL,
                listOf("Set the bench to a 30-45 degree incline.",
                    "Press the dumbbells up over the upper chest.",
                    "Lower slowly until you feel a stretch.",
                    "Press back to the top."),
                "Don't clang the dumbbells together at the top.", sets = 3, reps = "8-12"),
            ex("ch_pushup", "Push-Up", ExerciseType.MAIN, MuscleGroup.CHEST, Equipment.BODYWEIGHT,
                listOf("Start in a plank with hands under shoulders.",
                    "Lower your chest toward the floor.",
                    "Keep your body in a straight line.",
                    "Push back up to the start."),
                "Brace your core; avoid sagging hips.", sets = 3, reps = "12-20"),
            ex("ch_cable_fly", "Cable Fly", ExerciseType.MAIN, MuscleGroup.CHEST, Equipment.CABLE,
                listOf("Set cables to chest height.",
                    "Step forward with a slight forward lean.",
                    "Bring the handles together in front of you.",
                    "Return slowly with a slight elbow bend."),
                "Squeeze the chest at the midpoint.", sets = 3, reps = "12-15"),

            // ---------------- BACK ----------------
            ex("bk_pullup", "Pull-Up", ExerciseType.MAIN, MuscleGroup.BACK, Equipment.BODYWEIGHT,
                listOf("Hang from the bar with an overhand grip.",
                    "Pull your chest toward the bar.",
                    "Lead with the elbows and squeeze your back.",
                    "Lower under control."),
                "Avoid swinging; use a band for assistance if needed.", sets = 4, reps = "5-10"),
            ex("bk_bent_row", "Bent-Over Barbell Row", ExerciseType.MAIN, MuscleGroup.BACK, Equipment.BARBELL,
                listOf("Hinge at the hips with a flat back.",
                    "Let the bar hang at arm's length.",
                    "Row the bar to your lower ribs.",
                    "Lower with control."),
                "Keep your back neutral throughout.", sets = 4, reps = "8-10"),
            ex("bk_lat_pulldown", "Lat Pulldown", ExerciseType.MAIN, MuscleGroup.BACK, Equipment.CABLE,
                listOf("Sit and secure your thighs under the pad.",
                    "Grip the bar wider than shoulders.",
                    "Pull the bar to your upper chest.",
                    "Control the bar back up."),
                "Drive the elbows down and back.", sets = 3, reps = "10-12"),
            ex("bk_seated_row", "Seated Cable Row", ExerciseType.MAIN, MuscleGroup.BACK, Equipment.CABLE,
                listOf("Sit with feet braced and knees soft.",
                    "Grip the handle and sit tall.",
                    "Pull the handle to your stomach.",
                    "Extend the arms back slowly."),
                "Keep the torso still; don't yank with momentum.", sets = 3, reps = "10-12"),

            // ---------------- LEGS ----------------
            ex("lg_back_squat", "Barbell Back Squat", ExerciseType.MAIN, MuscleGroup.LEGS, Equipment.BARBELL,
                listOf("Rack the bar on your upper traps.",
                    "Brace and unrack, stepping back.",
                    "Sit down and back until thighs are parallel.",
                    "Drive up through the whole foot."),
                "Keep your core tight and chest proud.", sets = 4, reps = "6-10"),
            ex("lg_rdl", "Romanian Deadlift", ExerciseType.MAIN, MuscleGroup.LEGS, Equipment.BARBELL,
                listOf("Hold the bar at hip height.",
                    "Hinge at the hips, pushing them back.",
                    "Lower the bar along your legs.",
                    "Stand tall by squeezing the glutes."),
                "Feel the stretch in the hamstrings; keep a flat back.", sets = 3, reps = "8-12"),
            ex("lg_lunge", "Walking Lunge", ExerciseType.MAIN, MuscleGroup.LEGS, Equipment.DUMBBELL,
                listOf("Hold dumbbells at your sides.",
                    "Step forward and lower the back knee.",
                    "Push off the front heel to step through.",
                    "Alternate legs."),
                "Keep your torso upright and core engaged.", sets = 3, reps = "10 / leg"),
            ex("lg_leg_press", "Leg Press", ExerciseType.MAIN, MuscleGroup.LEGS, Equipment.MACHINE,
                listOf("Sit with feet shoulder-width on the platform.",
                    "Release the safeties and lower slowly.",
                    "Bend until knees reach ~90 degrees.",
                    "Press back without locking the knees."),
                "Don't let your lower back round off the seat.", sets = 3, reps = "10-15"),
            ex("lg_calf_raise", "Standing Calf Raise", ExerciseType.MAIN, MuscleGroup.LEGS, Equipment.MACHINE,
                listOf("Place the balls of your feet on the platform.",
                    "Lower the heels for a deep stretch.",
                    "Drive up onto your toes.",
                    "Pause and lower slowly."),
                "Use a full range of motion.", sets = 4, reps = "15-20"),

            // ---------------- SHOULDERS ----------------
            ex("sh_ohp", "Overhead Press", ExerciseType.MAIN, MuscleGroup.SHOULDERS, Equipment.BARBELL,
                listOf("Hold the bar at shoulder height.",
                    "Brace your core and glutes.",
                    "Press the bar overhead.",
                    "Lower back to the shoulders."),
                "Keep ribs down; don't over-arch the lower back.", sets = 4, reps = "6-10"),
            ex("sh_lateral_raise", "Lateral Raise", ExerciseType.MAIN, MuscleGroup.SHOULDERS, Equipment.DUMBBELL,
                listOf("Hold dumbbells at your sides.",
                    "Raise the arms out to shoulder height.",
                    "Lead with the elbows.",
                    "Lower slowly."),
                "Use light weight and avoid swinging.", sets = 3, reps = "12-15"),
            ex("sh_face_pull", "Face Pull", ExerciseType.MAIN, MuscleGroup.SHOULDERS, Equipment.CABLE,
                listOf("Set the cable to face height with a rope.",
                    "Pull the rope toward your face.",
                    "Flare the elbows high and squeeze.",
                    "Return with control."),
                "Great for shoulder health and posture.", sets = 3, reps = "12-15"),

            // ---------------- ARMS ----------------
            ex("ar_db_curl", "Dumbbell Biceps Curl", ExerciseType.MAIN, MuscleGroup.ARMS, Equipment.DUMBBELL,
                listOf("Hold dumbbells with palms forward.",
                    "Curl the weights to your shoulders.",
                    "Keep elbows pinned to your sides.",
                    "Lower slowly."),
                "No swinging; control the negative.", sets = 3, reps = "10-12"),
            ex("ar_tricep_pushdown", "Triceps Pushdown", ExerciseType.MAIN, MuscleGroup.ARMS, Equipment.CABLE,
                listOf("Grip the bar at a high cable.",
                    "Tuck elbows to your sides.",
                    "Push the bar down until arms extend.",
                    "Return with control."),
                "Only the forearms should move.", sets = 3, reps = "12-15"),
            ex("ar_hammer_curl", "Hammer Curl", ExerciseType.MAIN, MuscleGroup.ARMS, Equipment.DUMBBELL,
                listOf("Hold dumbbells with a neutral grip.",
                    "Curl up keeping palms facing in.",
                    "Squeeze at the top.",
                    "Lower under control."),
                "Targets the brachialis for thicker arms.", sets = 3, reps = "10-12"),

            // ---------------- CORE ----------------
            ex("co_plank", "Plank", ExerciseType.MAIN, MuscleGroup.CORE, Equipment.BODYWEIGHT,
                listOf("Rest on forearms and toes.",
                    "Keep your body in a straight line.",
                    "Brace your abs and glutes.",
                    "Hold and breathe."),
                "Don't let the hips sag or pike.", sets = 3, reps = "Hold", duration = 45),
            ex("co_hanging_raise", "Hanging Leg Raise", ExerciseType.MAIN, MuscleGroup.CORE, Equipment.BODYWEIGHT,
                listOf("Hang from a bar.",
                    "Raise your legs to hip height or higher.",
                    "Lower with control.",
                    "Avoid swinging."),
                "Curl the pelvis up to engage the lower abs.", sets = 3, reps = "10-15"),
            ex("co_russian_twist", "Russian Twist", ExerciseType.MAIN, MuscleGroup.CORE, Equipment.BODYWEIGHT,
                listOf("Sit with knees bent and lean back slightly.",
                    "Hold your hands or a weight at your chest.",
                    "Rotate the torso side to side.",
                    "Keep the core braced."),
                "Move from the ribs, not just the arms.", sets = 3, reps = "20 total"),

            // ---------------- GLUTES ----------------
            ex("gl_hip_thrust", "Barbell Hip Thrust", ExerciseType.MAIN, MuscleGroup.GLUTES, Equipment.BARBELL,
                listOf("Sit with upper back against a bench, bar over hips.",
                    "Drive through your heels to lift the hips.",
                    "Squeeze the glutes at the top.",
                    "Lower with control."),
                "Keep the chin tucked and ribs down.", sets = 4, reps = "8-12"),
            ex("gl_glute_bridge", "Glute Bridge", ExerciseType.MAIN, MuscleGroup.GLUTES, Equipment.BODYWEIGHT,
                listOf("Lie on your back, knees bent.",
                    "Drive the hips up.",
                    "Squeeze the glutes hard at the top.",
                    "Lower slowly."),
                "Great activation move before leg day.", sets = 3, reps = "15-20"),

            // ---------------- STRETCHES (timed) ----------------
            ex("st_child_pose", "Child's Pose", ExerciseType.STRETCH, MuscleGroup.MOBILITY, Equipment.BODYWEIGHT,
                listOf("Kneel and sit back on your heels.",
                    "Reach your arms forward and lower the chest.",
                    "Relax and breathe deeply."),
                "Lengthens the spine and relaxes the back.", reps = "Hold", duration = 45),
            ex("st_hamstring", "Standing Hamstring Stretch", ExerciseType.STRETCH, MuscleGroup.LEGS, Equipment.BODYWEIGHT,
                listOf("Place one heel on a low surface.",
                    "Keep the leg straight.",
                    "Hinge forward gently until you feel a stretch."),
                "Don't bounce; breathe into it.", reps = "Hold", duration = 40),
            ex("st_chest_doorway", "Doorway Chest Stretch", ExerciseType.STRETCH, MuscleGroup.CHEST, Equipment.BODYWEIGHT,
                listOf("Place forearms on a doorframe.",
                    "Step one foot through.",
                    "Lean forward to open the chest."),
                "Keep your shoulders down and relaxed.", reps = "Hold", duration = 40),
            ex("st_quad", "Standing Quad Stretch", ExerciseType.STRETCH, MuscleGroup.LEGS, Equipment.BODYWEIGHT,
                listOf("Stand on one leg, holding support.",
                    "Pull the other heel toward your glute.",
                    "Keep knees together and hips forward."),
                "Squeeze the glute for a deeper stretch.", reps = "Hold", duration = 40),
            ex("st_shoulder_cross", "Cross-Body Shoulder Stretch", ExerciseType.STRETCH, MuscleGroup.SHOULDERS, Equipment.BODYWEIGHT,
                listOf("Bring one arm across your chest.",
                    "Use the other arm to gently pull it closer.",
                    "Hold and switch sides."),
                "Keep the shoulder down, away from the ear.", reps = "Hold", duration = 30),
            ex("st_cobra", "Cobra Stretch", ExerciseType.STRETCH, MuscleGroup.CORE, Equipment.BODYWEIGHT,
                listOf("Lie face down with hands under shoulders.",
                    "Press the chest up, keeping hips down.",
                    "Breathe and relax the lower back."),
                "Stretches the abs and opens the front body.", reps = "Hold", duration = 30),
        )
    }

    // ---------------- DEFAULT WEEKLY PLAN ----------------
    val defaultDays: List<WorkoutDayEntity> by lazy {
        listOf(
            WorkoutDayEntity(DayOfWeek.MONDAY.value, "Push Day", "Chest, Shoulders & Triceps", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.TUESDAY.value, "Pull Day", "Back & Biceps", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.WEDNESDAY.value, "Leg Day", "Quads, Hamstrings & Calves", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.THURSDAY.value, "Core & Cardio", "Abs & Conditioning", 7 * 60, true),
            WorkoutDayEntity(DayOfWeek.FRIDAY.value, "Upper Body", "Chest, Back & Arms", 18 * 60, true),
            WorkoutDayEntity(DayOfWeek.SATURDAY.value, "Glutes & Legs", "Glutes & Lower Body", 10 * 60, true),
            WorkoutDayEntity(DayOfWeek.SUNDAY.value, "Rest & Recover", "Mobility & Stretching", -1, false, isRestDay = true),
        )
    }

    /** Builds default day-exercise assignments. order is auto-sequenced per section. */
    fun defaultPlan(): List<DayExerciseCrossRef> {
        val refs = mutableListOf<DayExerciseCrossRef>()
        fun assign(day: DayOfWeek, ids: List<Pair<String, ExerciseType>>) {
            val counters = mutableMapOf<ExerciseType, Int>()
            ids.forEach { (id, section) ->
                val order = counters.getOrDefault(section, 0)
                counters[section] = order + 1
                val src = exercises.first { it.id == id }
                refs += DayExerciseCrossRef(
                    dayOfWeek = day.value,
                    exerciseId = id,
                    section = section.name,
                    orderIndex = order,
                    sets = src.defaultSets,
                    reps = src.defaultReps
                )
            }
        }

        assign(DayOfWeek.MONDAY, listOf(
            "wu_jumping_jacks" to ExerciseType.WARMUP,
            "wu_arm_circles" to ExerciseType.WARMUP,
            "ch_bench_press" to ExerciseType.MAIN,
            "ch_incline_db_press" to ExerciseType.MAIN,
            "sh_ohp" to ExerciseType.MAIN,
            "sh_lateral_raise" to ExerciseType.MAIN,
            "ar_tricep_pushdown" to ExerciseType.MAIN,
            "st_chest_doorway" to ExerciseType.STRETCH,
            "st_shoulder_cross" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.TUESDAY, listOf(
            "wu_jumping_jacks" to ExerciseType.WARMUP,
            "wu_arm_circles" to ExerciseType.WARMUP,
            "bk_pullup" to ExerciseType.MAIN,
            "bk_bent_row" to ExerciseType.MAIN,
            "bk_lat_pulldown" to ExerciseType.MAIN,
            "ar_db_curl" to ExerciseType.MAIN,
            "ar_hammer_curl" to ExerciseType.MAIN,
            "st_child_pose" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.WEDNESDAY, listOf(
            "wu_bodyweight_squat" to ExerciseType.WARMUP,
            "wu_leg_swings" to ExerciseType.WARMUP,
            "lg_back_squat" to ExerciseType.MAIN,
            "lg_rdl" to ExerciseType.MAIN,
            "lg_leg_press" to ExerciseType.MAIN,
            "lg_calf_raise" to ExerciseType.MAIN,
            "st_hamstring" to ExerciseType.STRETCH,
            "st_quad" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.THURSDAY, listOf(
            "wu_high_knees" to ExerciseType.WARMUP,
            "wu_jumping_jacks" to ExerciseType.WARMUP,
            "co_plank" to ExerciseType.MAIN,
            "co_hanging_raise" to ExerciseType.MAIN,
            "co_russian_twist" to ExerciseType.MAIN,
            "st_cobra" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.FRIDAY, listOf(
            "wu_arm_circles" to ExerciseType.WARMUP,
            "wu_jumping_jacks" to ExerciseType.WARMUP,
            "ch_pushup" to ExerciseType.MAIN,
            "bk_seated_row" to ExerciseType.MAIN,
            "ch_cable_fly" to ExerciseType.MAIN,
            "ar_db_curl" to ExerciseType.MAIN,
            "ar_tricep_pushdown" to ExerciseType.MAIN,
            "st_chest_doorway" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.SATURDAY, listOf(
            "wu_bodyweight_squat" to ExerciseType.WARMUP,
            "gl_glute_bridge" to ExerciseType.WARMUP,
            "gl_hip_thrust" to ExerciseType.MAIN,
            "lg_lunge" to ExerciseType.MAIN,
            "lg_calf_raise" to ExerciseType.MAIN,
            "st_quad" to ExerciseType.STRETCH,
            "st_hamstring" to ExerciseType.STRETCH,
        ))
        assign(DayOfWeek.SUNDAY, listOf(
            "wu_cat_cow" to ExerciseType.WARMUP,
            "st_child_pose" to ExerciseType.STRETCH,
            "st_cobra" to ExerciseType.STRETCH,
            "st_hamstring" to ExerciseType.STRETCH,
            "st_shoulder_cross" to ExerciseType.STRETCH,
        ))
        return refs
    }
}
