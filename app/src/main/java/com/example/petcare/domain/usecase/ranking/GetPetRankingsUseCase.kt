package com.example.petcare.domain.usecase.ranking

import com.example.petcare.domain.model.RankedPet
import com.example.petcare.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetRankingsUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    operator fun invoke(): Flow<List<RankedPet>> = repository.getPetRankings()
}