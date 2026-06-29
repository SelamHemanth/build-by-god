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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WaterReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: WaterReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(NotificationConstants.EXTRA_WATER_INTERVAL, 90)
        val start = intent.getIntExtra(NotificationConstants.EXTRA_WATER_START, 8 * 60)
        val end = intent.getIntExtra(NotificationConstants.EXTRA_WATER_END, 22 * 60)
        val target = intent.getIntExtra(NotificationConstants.EXTRA_WATER_TARGET, 2500)

        postNotification(context, interval, start, end, target)

        // Arm the next slot (today's next, or tomorrow's first).
        WaterReminderScheduler.nextTrigger(interval, start, end)?.let { next ->
            scheduler.scheduleAt(next, interval, start, end, target)
        }
    }

    private fun postNotification(context: Context, interval: Int, start: Int, end: Int, target: Int) {
        val slots = WaterReminderScheduler.slotCount(interval, start, end).coerceAtLeast(1)
        val perGlass = (target / slots).coerceAtLeast(50)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            NotificationConstants.WATER_REQUEST_CODE,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationConstants.WATER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to hydrate")
            .setContentText("Drink about $perGlass ml now to reach your $target ml goal today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(NotificationConstants.WATER_NOTIFICATION_ID, notification)
        }
    }
}
