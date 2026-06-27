# Default ProGuard rules. Keep Hilt/Room generated code intact.
-keepattributes *Annotation*
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
