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
    suspend fun update(transform: (UserProfile) -> UserProfile) = store.update(transform)
}
