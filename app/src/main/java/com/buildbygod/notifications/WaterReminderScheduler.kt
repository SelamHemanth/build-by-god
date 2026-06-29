package com.buildbygod.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules water-intake reminders at a fixed interval inside a daily [start, end] window.
 * Android alarms aren't natively windowed/repeating-with-bounds, so we schedule the next slot
 * and the receiver re-arms the following one after it fires.
 */
@Singleton
class WaterReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun reschedule(
        enabled: Boolean,
        intervalMinutes: Int,
        startMinutes: Int,
        endMinutes: Int,
        dailyTargetMl: Int
    ) {
        cancel()
        if (!enabled || intervalMinutes <= 0 || endMinutes <= startMinutes) return
        val triggerAt = nextTrigger(intervalMinutes, startMinutes, endMinutes) ?: return
        scheduleAt(triggerAt, intervalMinutes, startMinutes, endMinutes, dailyTargetMl)
    }

    /** Used by the receiver to arm the next slot after one fires. */
    fun scheduleAt(
        triggerAtMillis: Long,
        intervalMinutes: Int,
        startMinutes: Int,
        endMinutes: Int,
        dailyTargetMl: Int
    ) {
        val pending = buildPendingIntent(intervalMinutes, startMinutes, endMinutes, dailyTargetMl)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    fun cancel() {
        PendingIntent.getBroadcast(
            context,
            NotificationConstants.WATER_REQUEST_CODE,
            Intent(context, WaterReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it) }
    }

    private fun buildPendingIntent(
        intervalMinutes: Int,
        startMinutes: Int,
        endMinutes: Int,
        dailyTargetMl: Int
    ): PendingIntent {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_WATER_INTERVAL, intervalMinutes)
            putExtra(NotificationConstants.EXTRA_WATER_START, startMinutes)
            putExtra(NotificationConstants.EXTRA_WATER_END, endMinutes)
            putExtra(NotificationConstants.EXTRA_WATER_TARGET, dailyTargetMl)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationConstants.WATER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        /** Number of reminder slots that fit in the window, inclusive of the start. */
        fun slotCount(intervalMinutes: Int, startMinutes: Int, endMinutes: Int): Int {
            if (intervalMinutes <= 0 || endMinutes <= startMinutes) return 1
            return ((endMinutes - startMinutes) / intervalMinutes) + 1
        }

        /** Next slot time (epoch millis) at/after now, wrapping to tomorrow's start if past the window. */
        fun nextTrigger(
            intervalMinutes: Int,
            startMinutes: Int,
            endMinutes: Int,
            now: LocalDateTime = LocalDateTime.now()
        ): Long? {
            if (intervalMinutes <= 0 || endMinutes <= startMinutes) return null
            val nowMin = now.hour * 60 + now.minute
            val zone = ZoneId.systemDefault()

            fun atMinutes(date: LocalDate, minutes: Int): Long =
                LocalDateTime.of(date, LocalTime.of(minutes / 60, minutes % 60))
                    .atZone(zone).toInstant().toEpochMilli()

            // Before today's window -> first slot today.
            if (nowMin <= startMinutes) return atMinutes(now.toLocalDate(), startMinutes)

            // Within window -> next aligned slot strictly after now.
            if (nowMin < endMinutes) {
                val elapsed = nowMin - startMinutes
                val slotsPassed = (elapsed / intervalMinutes) + 1
                val nextMin = startMinutes + slotsPassed * intervalMinutes
                if (nextMin <= endMinutes) return atMinutes(now.toLocalDate(), nextMin)
            }

            // Past the window -> tomorrow's start.
            return atMinutes(now.toLocalDate().plusDays(1), startMinutes)
        }
    }
}
