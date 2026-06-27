package com.buildbygod.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.buildbygod.MainActivity
import com.buildbygod.R
import com.buildbygod.data.local.entity.WorkoutDayEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getIntExtra(NotificationConstants.EXTRA_DAY, -1)
        val title = intent.getStringExtra(NotificationConstants.EXTRA_TITLE) ?: "Workout time"

        postNotification(context, day, title)

        // Re-arm for next week.
        if (day in 1..7) {
            CoroutineScope(Dispatchers.Default).launch {
                scheduler.schedule(
                    WorkoutDayEntity(
                        dayOfWeek = day,
                        title = title,
                        focus = "",
                        scheduledMinutes = nowMinutesFallback(intent),
                        reminderEnabled = true
                    )
                )
            }
        }
    }

    private fun nowMinutesFallback(intent: Intent): Int =
        intent.getIntExtra("minutes", java.time.LocalTime.now().toSecondOfDay() / 60)

    private fun postNotification(context: Context, day: Int, title: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_DAY, day.toString())
        }
        val pending = PendingIntent.getActivity(
            context,
            1000 + day,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for $title")
            .setContentText("Your workout is ready. Let's forge it. Tap to begin.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(2000 + day, notification)
        }
    }
}
