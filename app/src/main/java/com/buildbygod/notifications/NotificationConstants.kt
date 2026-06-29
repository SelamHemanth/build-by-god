package com.buildbygod.notifications

object NotificationConstants {
    const val CHANNEL_ID = "workout_reminders"
    const val WATER_CHANNEL_ID = "water_reminders"
    const val EXTRA_DAY = "extra_day_of_week"
    const val EXTRA_TITLE = "extra_title"
    /** Optional destination route (e.g. "diet") to open directly from a widget/notification. */
    const val EXTRA_ROUTE = "extra_route"

    // Water reminder extras
    const val EXTRA_WATER_INTERVAL = "extra_water_interval"
    const val EXTRA_WATER_START = "extra_water_start"
    const val EXTRA_WATER_END = "extra_water_end"
    const val EXTRA_WATER_TARGET = "extra_water_target"

    const val WATER_REQUEST_CODE = 7700
    const val WATER_NOTIFICATION_ID = 7701
}
