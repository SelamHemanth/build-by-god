package com.buildbygod.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.buildbygod.MainActivity
import com.buildbygod.R
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.notifications.NotificationConstants
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Home-screen widget that shows today's planned workout and opens it on tap. */
class TodayWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun workoutDao(): WorkoutDao
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
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .workoutDao()

        val today = LocalDate.now().dayOfWeek
        val day = today.value
        val entity = dao.getDay(day)
        val count = dao.countDayExercises(day)
        val dayLabel = today.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())

        val views = RemoteViews(context.packageName, R.layout.widget_today).apply {
            setTextViewText(R.id.widget_eyebrow, "TODAY  ·  $dayLabel")
            val isRest = entity?.isRestDay ?: false
            when {
                entity == null -> {
                    setTextViewText(R.id.widget_title, "Build By God")
                    setTextViewText(R.id.widget_subtitle, "Open the app to set up your plan")
                    setViewVisibility(R.id.widget_cta, View.GONE)
                }
                isRest -> {
                    setTextViewText(R.id.widget_title, entity.title)
                    setTextViewText(R.id.widget_subtitle, entity.focus)
                    setViewVisibility(R.id.widget_cta, View.GONE)
                }
                else -> {
                    setTextViewText(R.id.widget_title, entity.title)
                    val exTxt = if (count > 0) "${entity.focus}  ·  $count exercises" else entity.focus
                    setTextViewText(R.id.widget_subtitle, exTxt)
                    setViewVisibility(R.id.widget_cta, View.VISIBLE)
                    setTextViewText(R.id.widget_cta, "Start workout  ›")
                }
            }

            val tap = openDayIntent(context, day)
            setOnClickPendingIntent(R.id.widget_root, tap)
            setOnClickPendingIntent(R.id.widget_cta, tap)
        }

        manager.updateAppWidget(ids, views)
    }

    private fun openDayIntent(context: Context, day: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DAY, day.toString())
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(
            context,
            1000 + day,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Ask the system to refresh every placed widget (e.g. after the plan changes). */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, TodayWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
