package com.example.petcare.data.repository

import com.example.petcare.data.datasource.MockRankingDataSource
import com.example.petcare.domain.model.RankedPet
import com.example.petcare.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepositoryImpl @Inject constructor(
    private val mockRankingDataSource: MockRankingDataSource
) : RankingRepository {

    override fun getPetRankings(): Flow<List<RankedPet>> {
        return mockRankingDataSource.getRankings()
    }
}