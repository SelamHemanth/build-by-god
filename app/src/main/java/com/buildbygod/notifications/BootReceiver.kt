package com.buildbygod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buildbygod.data.datastore.WaterReminderStore
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.domain.model.NutritionCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reschedules all reminders after a reboot or app update. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var workoutDao: WorkoutDao
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var waterStore: WaterReminderStore
    @Inject lateinit var waterScheduler: WaterReminderScheduler
    @Inject lateinit var profileRepo: ProfileRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    scheduler.rescheduleAll(workoutDao.daysWithReminders())

                    val prefs = waterStore.prefs.first()
                    if (prefs.enabled) {
                        val profile = profileRepo.profile.first()
                        val target = waterTarget(profile)
                        waterScheduler.reschedule(
                            enabled = true,
                            intervalMinutes = prefs.intervalMinutes,
                            startMinutes = prefs.startMinutes,
                            endMinutes = prefs.endMinutes,
                            dailyTargetMl = target
                        )
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }

    private fun waterTarget(profile: com.buildbygod.data.datastore.UserProfile): Int {
        val weight = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight
        return if (NutritionCalculator.isComplete(weight, profile.heightCm, profile.age)) {
            NutritionCalculator.compute(
                profile.sex, weight, profile.heightCm, profile.age,
                profile.activityLevel, profile.primaryGoal
            ).waterMl
        } else 2500
    }
}
