package com.buildbygod.data.repository

import com.buildbygod.data.datastore.ProfileDataStore
import com.buildbygod.data.datastore.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val store: ProfileDataStore
) {
    val profile: Flow<UserProfile> = store.profile
    val users: Flow<List<UserProfile>> = store.users
    suspend fun update(transform: (UserProfile) -> UserProfile) = store.update(transform)
    suspend fun addUser(name: String) = store.addUser(name)
    suspend fun switchUser(id: String) = store.switchUser(id)
    suspend fun removeUser(id: String) = store.removeUser(id)
}
