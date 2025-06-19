package com.example.petcare.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.petcare.domain.model.PetGender
import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.repository.PetProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val PET_PROFILE_PREFERENCES = "pet_profile_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PET_PROFILE_PREFERENCES)

@Singleton
class PetProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PetProfileRepository {

    private object PreferencesKeys {
        val ID = stringPreferencesKey("pet_id")
        val NAME = stringPreferencesKey("pet_name")
        val BREED = stringPreferencesKey("pet_breed")
        val AGE = intPreferencesKey("pet_age")
        val GENDER = stringPreferencesKey("pet_gender")
        val WEIGHT = doublePreferencesKey("pet_weight")
        val DATE_OF_BIRTH = longPreferencesKey("pet_date_of_birth")
        val AVATAR_URL = stringPreferencesKey("pet_avatar_url")
    }

    override fun getPetProfile(): Flow<PetProfile?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.NAME]?.let { name ->
                    PetProfile(
                        id = preferences[PreferencesKeys.ID] ?: "default_pet", // Provide a default
                        name = name,
                        breed = preferences[PreferencesKeys.BREED] ?: "",
                        age = preferences[PreferencesKeys.AGE] ?: 0,
                        gender = PetGender.valueOf(preferences[PreferencesKeys.GENDER] ?: "UNKNOWN"),
                        weight = preferences[PreferencesKeys.WEIGHT] ?: 0.0,
                        dateOfBirthTimestamp = preferences[PreferencesKeys.DATE_OF_BIRTH] ?: 0L,
                        avatarUrl = preferences[PreferencesKeys.AVATAR_URL]
                    )
                }
            }
    }

    override suspend fun savePetProfile(profile: PetProfile) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ID] = profile.id
            preferences[PreferencesKeys.NAME] = profile.name
            preferences[PreferencesKeys.BREED] = profile.breed
            preferences[PreferencesKeys.AGE] = profile.age
            preferences[PreferencesKeys.GENDER] = profile.gender.name
            preferences[PreferencesKeys.WEIGHT] = profile.weight
            preferences[PreferencesKeys.DATE_OF_BIRTH] = profile.dateOfBirthTimestamp
            profile.avatarUrl?.let { preferences[PreferencesKeys.AVATAR_URL] = it }
        }
    }
    override suspend fun updatePetName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.NAME] = name }
    }
    override suspend fun hasProfile(): Boolean = getPetProfile().map { it != null }.catch { emit(false) }.first()
}