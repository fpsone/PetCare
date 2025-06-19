package com.example.petcare.domain.repository

import com.example.petcare.domain.model.PetProfile
import kotlinx.coroutines.flow.Flow

interface PetProfileRepository {
    fun getPetProfile(): Flow<PetProfile?>
    suspend fun savePetProfile(profile: PetProfile)
    suspend fun updatePetName(name: String) // Example of a specific update
    // Add other specific update methods as needed, or a general update(profile: PetProfile)
    suspend fun hasProfile(): Boolean
}