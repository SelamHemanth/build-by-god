package com.buildbygod.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentViolet
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buildbygod.ui.components.GlossyBackground
import com.buildbygod.ui.daydetail.DayDetailScreen
import com.buildbygod.ui.exercise.ExerciseDetailScreen
import com.buildbygod.ui.home.HomeScreen
import com.buildbygod.ui.library.LibraryScreen
import com.buildbygod.ui.navigation.BuildByGodBottomBar
import com.buildbygod.ui.navigation.Routes
import com.buildbygod.ui.navigation.TopLevel
import com.buildbygod.ui.onboarding.OnboardingScreen
import com.buildbygod.ui.plan.PlanScreen
import com.buildbygod.ui.profile.ProfileScreen
import com.buildbygod.ui.progress.ProgressScreen
import com.buildbygod.ui.session.SessionScreen
import com.buildbygod.ui.settings.SettingsScreen

@Composable
fun BuildByGodRoot(
    startDayDeepLink: String?,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val state by appViewModel.appState.collectAsStateWithLifecycle()

    GlossyBackground {
        Crossfade(targetState = state, label = "appState") { s ->
            when (s) {
                AppState.Loading -> BrandSplash()
                AppState.Onboarding -> OnboardingScreen()
                AppState.Ready -> MainScaffold(startDayDeepLink)
            }
        }
    }
}

@Composable
private fun BrandSplash() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Build By God",
                style = MaterialTheme.typography.displaySmall,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                "powered by hemanth",
                style = MaterialTheme.typography.labelLarge,
                color = AccentViolet,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator(color = AccentBlue)
        }
    }
}

@Composable
private fun MainScaffold(startDayDeepLink: String?) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    LaunchedEffect(startDayDeepLink) {
        val day = startDayDeepLink?.toIntOrNull()
        if (day != null && day in 1..7) {
            navController.navigate(Routes.dayDetail(day))
        }
    }

    val showBar = currentRoute in TopLevel.entries.map { it.route }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBar) {
                BuildByGodBottomBar(currentRoute = currentRoute) { top ->
                    navController.navigate(top.route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().widthIn(max = 720.dp)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenDay = { navController.navigate(Routes.dayDetail(it)) },
                    onStartSession = { navController.navigate(Routes.session(it)) }
                )
            }
            composable(Routes.PLAN) {
                PlanScreen(onOpenDay = { navController.navigate(Routes.dayDetail(it)) })
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(onOpenExercise = { navController.navigate(Routes.exerciseDetail(it)) })
            }
            composable(Routes.PROGRESS) { ProgressScreen() }
            composable(Routes.PROFILE) {
                ProfileScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Routes.DAY_DETAIL,
                arguments = listOf(navArgument("day") { type = NavType.IntType })
            ) { entry ->
                val day = entry.arguments?.getInt("day") ?: 1
                DayDetailScreen(
                    day = day,
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { navController.navigate(Routes.exerciseDetail(it)) },
                    onStartSession = { navController.navigate(Routes.session(day)) }
                )
            }
            composable(
                Routes.EXERCISE_DETAIL,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) {
                ExerciseDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Routes.SESSION,
                arguments = listOf(navArgument("day") { type = NavType.IntType })
            ) { entry ->
                val day = entry.arguments?.getInt("day") ?: 1
                SessionScreen(day = day, onFinish = { navController.popBackStack() })
            }
        }
        }
    }
}
