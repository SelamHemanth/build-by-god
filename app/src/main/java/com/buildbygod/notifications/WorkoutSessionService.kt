package com.buildbygod.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.buildbygod.MainActivity
import com.buildbygod.R

/**
 * Keeps an ongoing notification (workout title, current exercise, live timer) alive while a session
 * is in progress, so the user still sees their progress after leaving the app or locking the screen.
 *
 * Uses a chronometer so the elapsed time ticks without us posting every second.
 */
class WorkoutSessionService : Service() {

    private var startedAt = 0L
    private var title = "Workout"
    private var exercise = ""
    private var position = 0
    private var total = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active workout",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows your live workout timer and current exercise."
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (startedAt == 0L) {
            startedAt = intent?.getLongExtra(EXTRA_STARTED_AT, System.currentTimeMillis())
                ?: System.currentTimeMillis()
        }
        intent?.getStringExtra(EXTRA_TITLE)?.let { title = it }
        intent?.getStringExtra(EXTRA_EXERCISE)?.let { exercise = it }
        position = intent?.getIntExtra(EXTRA_POSITION, position) ?: position
        total = intent?.getIntExtra(EXTRA_TOTAL, total) ?: total

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progressText = if (total > 0) "$exercise  ·  $position/$total" else exercise

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(progressText)
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setWhen(startedAt)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 4242
        const val CHANNEL_ID = "workout_session"

        private const val ACTION_START = "com.buildbygod.session.START"
        private const val ACTION_STOP = "com.buildbygod.session.STOP"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_EXERCISE = "exercise"
        private const val EXTRA_POSITION = "position"
        private const val EXTRA_TOTAL = "total"
        private const val EXTRA_STARTED_AT = "startedAt"

        /** Start (or refresh) the live workout notification. */
        fun start(context: Context, title: String, exercise: String, position: Int, total: Int, startedAt: Long) {
            val intent = Intent(context, WorkoutSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_EXERCISE, exercise)
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_TOTAL, total)
                putExtra(EXTRA_STARTED_AT, startedAt)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WorkoutSessionService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
