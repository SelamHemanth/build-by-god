package com.buildbygod.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.buildbygod.MainActivity
import com.buildbygod.R
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.domain.model.NutritionCalculator
import com.buildbygod.notifications.NotificationConstants
import com.buildbygod.ui.navigation.Routes
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Compact home-screen widget that shows the user's daily calorie + protein target and opens the diet plan. */
class DietWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DietWidgetEntryPoint {
        fun profileRepository(): ProfileRepository
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        scope.launch {
            try {
                render(context, manager, ids)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val profileRepo = EntryPointAccessors
            .fromApplication(context.applicationContext, DietWidgetEntryPoint::class.java)
            .profileRepository()

        val profile = profileRepo.profile.first()
        val complete = NutritionCalculator.isComplete(profile.weightKg, profile.heightCm, profile.age)
        val (kcal, protein) = if (complete) {
            val plan = NutritionCalculator.compute(
                profile.sex, profile.weightKg, profile.heightCm, profile.age,
                profile.activityLevel, profile.primaryGoal
            )
            plan.calorieTarget to plan.proteinG
        } else {
            2000 to 120
        }

        val views = RemoteViews(context.packageName, R.layout.widget_diet).apply {
            setTextViewText(R.id.widget_diet_eyebrow, if (complete) "DIET · TARGET" else "DIET · EST.")
            setTextViewText(R.id.widget_diet_calories, "$kcal kcal")
            setTextViewText(R.id.widget_diet_macros, "${protein}g protein")
            val tap = openDietIntent(context)
            setOnClickPendingIntent(R.id.widget_diet_root, tap)
            setOnClickPendingIntent(R.id.widget_diet_cta, tap)
        }
        manager.updateAppWidget(ids, views)
    }

    private fun openDietIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(NotificationConstants.EXTRA_ROUTE, Routes.DIET)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(
            context,
            2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, DietWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, DietWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
