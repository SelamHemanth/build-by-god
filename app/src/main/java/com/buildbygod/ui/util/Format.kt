package com.buildbygod.ui.util

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

fun minutesToTimeLabel(minutes: Int): String {
    if (minutes < 0) return "Not set"
    val t = LocalTime.of((minutes / 60) % 24, minutes % 60)
    val hour12 = if (t.hour % 12 == 0) 12 else t.hour % 12
    val ampm = if (t.hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(hour12, t.minute, ampm)
}

fun dayShort(dayOfWeek: Int): String =
    DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())

fun dayFull(dayOfWeek: Int): String =
    DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())
