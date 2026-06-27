package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.Units
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.profileDataStore by preferencesDataStore(name = "profile")

data class UserProfile(
    val onboarded: Boolean = false,
    val name: String = "",
    val photoUri: String? = null,
    val goal: Goal = Goal.STAY_FIT,
    val units: Units = Units.METRIC,
    val defaultReminderLead: Int = 30,
    val heightCm: Int = 0,
    val startWeight: Float = 0f,
    val weightKg: Float = 0f,
    val age: Int = 0,
    val sex: Sex = Sex.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE
)

@Singleton
class ProfileDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val PHOTO = stringPreferencesKey("photo")
        val GOAL = stringPreferencesKey("goal")
        val UNITS = stringPreferencesKey("units")
        val LEAD = intPreferencesKey("reminder_lead")
        val HEIGHT = intPreferencesKey("height")
        val START_WEIGHT = stringPreferencesKey("start_weight")
        val WEIGHT = stringPreferencesKey("weight")
        val AGE = intPreferencesKey("age")
        val SEX = stringPreferencesKey("sex")
        val ACTIVITY = stringPreferencesKey("activity")
    }

    private fun read(p: androidx.datastore.preferences.core.Preferences) = UserProfile(
        onboarded = p[Keys.ONBOARDED] ?: false,
        name = p[Keys.NAME] ?: "",
        photoUri = p[Keys.PHOTO],
        goal = Goal.entries.firstOrNull { it.name == p[Keys.GOAL] } ?: Goal.STAY_FIT,
        units = Units.entries.firstOrNull { it.name == p[Keys.UNITS] } ?: Units.METRIC,
        defaultReminderLead = p[Keys.LEAD] ?: 30,
        heightCm = p[Keys.HEIGHT] ?: 0,
        startWeight = p[Keys.START_WEIGHT]?.toFloatOrNull() ?: 0f,
        weightKg = p[Keys.WEIGHT]?.toFloatOrNull() ?: 0f,
        age = p[Keys.AGE] ?: 0,
        sex = Sex.fromName(p[Keys.SEX]),
        activityLevel = ActivityLevel.fromName(p[Keys.ACTIVITY])
    )

    val profile: Flow<UserProfile> = context.profileDataStore.data.map { read(it) }

    suspend fun update(transform: (UserProfile) -> UserProfile) {
        context.profileDataStore.edit { prefs ->
            val updated = transform(read(prefs))
            prefs[Keys.ONBOARDED] = updated.onboarded
            prefs[Keys.NAME] = updated.name
            updated.photoUri?.let { prefs[Keys.PHOTO] = it }
            prefs[Keys.GOAL] = updated.goal.name
            prefs[Keys.UNITS] = updated.units.name
            prefs[Keys.LEAD] = updated.defaultReminderLead
            prefs[Keys.HEIGHT] = updated.heightCm
            prefs[Keys.START_WEIGHT] = updated.startWeight.toString()
            prefs[Keys.WEIGHT] = updated.weightKg.toString()
            prefs[Keys.AGE] = updated.age
            prefs[Keys.SEX] = updated.sex.name
            prefs[Keys.ACTIVITY] = updated.activityLevel.name
        }
    }
}
