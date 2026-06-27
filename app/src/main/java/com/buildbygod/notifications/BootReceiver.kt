package com.buildbygod.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buildbygod.data.local.dao.WorkoutDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reschedules all reminders after a reboot or app update. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var workoutDao: WorkoutDao
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    scheduler.rescheduleAll(workoutDao.daysWithReminders())
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
