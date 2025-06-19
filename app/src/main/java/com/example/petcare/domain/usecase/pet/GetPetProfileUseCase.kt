package com.example.petcare.domain.usecase.pet

import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.repository.PetProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetProfileUseCase @Inject constructor(
    private val repository: PetProfileRepository
) {
    operator fun invoke(): Flow<PetProfile?> = repository.getPetProfile()
}