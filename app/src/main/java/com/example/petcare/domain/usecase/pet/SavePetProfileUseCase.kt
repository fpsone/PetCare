package com.example.petcare.domain.usecase.pet

import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.repository.PetProfileRepository
import javax.inject.Inject

class SavePetProfileUseCase @Inject constructor(
    private val repository: PetProfileRepository
) {
    suspend operator fun invoke(profile: PetProfile) = repository.savePetProfile(profile)
}