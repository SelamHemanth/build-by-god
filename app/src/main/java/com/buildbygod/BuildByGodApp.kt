package com.buildbygod

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.buildbygod.notifications.NotificationConstants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BuildByGodApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /** Coil loader with animated-image (GIF/WebP) support for bundled exercise demo clips. */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()

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

        val waterChannel = NotificationChannel(
            NotificationConstants.WATER_CHANNEL_ID,
            getString(R.string.water_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.water_channel_desc)
        }
        manager.createNotificationChannel(waterChannel)
    }
}
