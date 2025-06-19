package com.example.petcare.domain.usecase.pet

import com.example.petcare.domain.repository.PetProfileRepository
import javax.inject.Inject

class CheckIfProfileExistsUseCase @Inject constructor(
    private val repository: PetProfileRepository
) {
    suspend operator fun invoke(): Boolean = repository.hasProfile()
}