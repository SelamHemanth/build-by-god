package com.buildbygod

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.buildbygod.notifications.NotificationConstants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BuildByGodApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            NotificationConstants.CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.reminder_channel_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
