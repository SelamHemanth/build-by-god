package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.HeightUnit
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

/** Multi-user roster lives here; each user keeps their own profile, switched via [UserProfile.id]. */
val Context.usersDataStore by preferencesDataStore(name = "users")

data class UserProfile(
    val id: String = "u1",
    val onboarded: Boolean = false,
    val name: String = "",
    val photoUri: String? = null,
    val profileFrame: Int = 0,
    val goals: Set<Goal> = setOf(Goal.STAY_FIT),
    val heightUnit: HeightUnit = HeightUnit.CM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val defaultReminderLead: Int = 30,
    val heightCm: Int = 0,
    val startWeight: Float = 0f,
    val weightKg: Float = 0f,
    /** Epoch day of birth (0 = unset). Age is derived. */
    val dobEpochDay: Long = 0,
    val sex: Sex = Sex.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val experience: ExperienceLevel = ExperienceLevel.BEGINNER
) {
    /** Age in whole years, derived from [dobEpochDay]. */
    val age: Int
        get() = if (dobEpochDay > 0)
            Period.between(LocalDate.ofEpochDay(dobEpochDay), LocalDate.now()).years.coerceAtLeast(0)
        else 0

    /** First selected goal, used where a single goal is needed (e.g. calorie target). */
    val primaryGoal: Goal get() = goals.firstOrNull() ?: Goal.STAY_FIT
}

@Singleton
class ProfileDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val USERS = stringPreferencesKey("users_json")
        val ACTIVE = stringPreferencesKey("active_id")
    }

    /** All saved users; never empty (a default user is synthesized when none exist). */
    val users: Flow<List<UserProfile>> = context.usersDataStore.data.map { parse(it).first }

    /** The currently selected user's profile. */
    val profile: Flow<UserProfile> = context.usersDataStore.data.map { p ->
        val (list, active) = parse(p)
        list.firstOrNull { it.id == active } ?: list.first()
    }

    /** Update the active user. */
    suspend fun update(transform: (UserProfile) -> UserProfile) {
        context.usersDataStore.edit { p ->
            val (list, active) = parse(p)
            val idx = list.indexOfFirst { it.id == active }.coerceAtLeast(0)
            val updated = transform(list[idx]).copy(id = list[idx].id)
            val newList = list.toMutableList().also { it[idx] = updated }
            write(p, newList, active)
        }
    }

    suspend fun addUser(name: String): String {
        val id = "u${System.currentTimeMillis()}"
        context.usersDataStore.edit { p ->
            val (list, _) = parse(p)
            val newUser = UserProfile(id = id, name = name.trim(), onboarded = false)
            write(p, list + newUser, id) // switch to the newcomer so they can onboard
        }
        return id
    }

    suspend fun switchUser(id: String) {
        context.usersDataStore.edit { p ->
            val (list, active) = parse(p)
            if (list.any { it.id == id }) write(p, list, id) else write(p, list, active)
        }
    }

    suspend fun removeUser(id: String) {
        context.usersDataStore.edit { p ->
            val (list, active) = parse(p)
            if (list.size <= 1) return@edit
            val newList = list.filterNot { it.id == id }
            val newActive = if (active == id) newList.first().id else active
            write(p, newList, newActive)
        }
    }

    // ---- serialization ----

    private fun write(p: androidx.datastore.preferences.core.MutablePreferences, list: List<UserProfile>, active: String) {
        p[Keys.USERS] = JSONArray(list.map { toJson(it) }).toString()
        p[Keys.ACTIVE] = active
    }

    private fun parse(p: Preferences): Pair<List<UserProfile>, String> {
        val json = p[Keys.USERS]
        if (json.isNullOrBlank()) {
            val def = UserProfile(id = "u1")
            return listOf(def) to def.id
        }
        val arr = JSONArray(json)
        val list = (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        if (list.isEmpty()) {
            val def = UserProfile(id = "u1")
            return listOf(def) to def.id
        }
        val active = p[Keys.ACTIVE]?.takeIf { id -> list.any { it.id == id } } ?: list.first().id
        return list to active
    }

    private fun toJson(u: UserProfile): JSONObject = JSONObject().apply {
        put("id", u.id)
        put("onboarded", u.onboarded)
        put("name", u.name)
        put("photoUri", u.photoUri ?: JSONObject.NULL)
        put("profileFrame", u.profileFrame)
        put("goals", JSONArray(u.goals.map { it.name }))
        put("heightUnit", u.heightUnit.name)
        put("weightUnit", u.weightUnit.name)
        put("defaultReminderLead", u.defaultReminderLead)
        put("heightCm", u.heightCm)
        put("startWeight", u.startWeight.toDouble())
        put("weightKg", u.weightKg.toDouble())
        put("dobEpochDay", u.dobEpochDay)
        put("sex", u.sex.name)
        put("activityLevel", u.activityLevel.name)
        put("experience", u.experience.name)
    }

    private fun fromJson(o: JSONObject): UserProfile {
        val goalsArr = o.optJSONArray("goals")
        val goals = if (goalsArr == null || goalsArr.length() == 0) setOf(Goal.STAY_FIT)
        else (0 until goalsArr.length())
            .mapNotNull { i -> Goal.entries.firstOrNull { it.name == goalsArr.optString(i) } }
            .toSet()
            .ifEmpty { setOf(Goal.STAY_FIT) }
        val photo = if (o.isNull("photoUri")) null else o.optString("photoUri", "").ifBlank { null }
        return UserProfile(
            id = o.optString("id", "u1"),
            onboarded = o.optBoolean("onboarded", false),
            name = o.optString("name", ""),
            photoUri = photo,
            profileFrame = o.optInt("profileFrame", 0),
            goals = goals,
            heightUnit = HeightUnit.fromName(o.optString("heightUnit")),
            weightUnit = WeightUnit.fromName(o.optString("weightUnit")),
            defaultReminderLead = o.optInt("defaultReminderLead", 30),
            heightCm = o.optInt("heightCm", 0),
            startWeight = o.optDouble("startWeight", 0.0).toFloat(),
            weightKg = o.optDouble("weightKg", 0.0).toFloat(),
            dobEpochDay = o.optLong("dobEpochDay", 0),
            sex = Sex.fromName(o.optString("sex")),
            activityLevel = ActivityLevel.fromName(o.optString("activityLevel")),
            experience = ExperienceLevel.fromName(o.optString("experience"))
        )
    }
}
