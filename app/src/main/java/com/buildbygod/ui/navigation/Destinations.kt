package com.buildbygod.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val PLAN = "plan"
    const val LIBRARY = "library"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    const val SETTINGS = "settings"
    const val MANAGE_PROFILE = "manageProfile"
    const val BREATHING = "breathing"
    const val DIET = "diet"
    const val DAY_DETAIL = "day/{day}"
    const val EXERCISE_DETAIL = "exercise/{exerciseId}"
    const val SESSION = "session/{day}"

    fun dayDetail(day: Int) = "day/$day"
    fun exerciseDetail(id: String) = "exercise/$id"
    fun session(day: Int) = "session/$day"
}

enum class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Filled.Home),
    PLAN(Routes.PLAN, "Plan", Icons.Filled.CalendarMonth),
    LIBRARY(Routes.LIBRARY, "Library", Icons.Filled.FitnessCenter),
    PROGRESS(Routes.PROGRESS, "Progress", Icons.Filled.BarChart),
    PROFILE(Routes.PROFILE, "Profile", Icons.Filled.Person)
}
