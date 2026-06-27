package com.buildbygod.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.buildbygod.data.local.entity.WorkoutDayEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules weekly exact alarms for each enabled workout day. */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    fun rescheduleAll(days: List<WorkoutDayEntity>) {
        days.forEach { schedule(it) }
    }

    fun schedule(day: WorkoutDayEntity) {
        if (!day.reminderEnabled || day.isRestDay || day.scheduledMinutes < 0) {
            cancel(day.dayOfWeek)
            return
        }
        val triggerAt = nextTrigger(day.dayOfWeek, day.scheduledMinutes)
        val pending = buildPendingIntent(day)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(dayOfWeek: Int) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                dayOfWeek,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: return
        )
    }

    private fun buildPendingIntent(day: WorkoutDayEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DAY, day.dayOfWeek)
            putExtra(NotificationConstants.EXTRA_TITLE, day.title)
        }
        return PendingIntent.getBroadcast(
            context,
            day.dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTrigger(dayOfWeek: Int, minutes: Int): Long {
        val now = LocalDateTime.now()
        val target = DayOfWeek.of(dayOfWeek)
        var dt = now
            .with(TemporalAdjusters.nextOrSame(target))
            .withHour(minutes / 60)
            .withMinute(minutes % 60)
            .withSecond(0)
            .withNano(0)
        if (dt.isBefore(now)) {
            dt = dt.with(TemporalAdjusters.next(target))
        }
        return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
