package com.example.petcare.domain.repository

import com.example.petcare.domain.model.RankedPet
import kotlinx.coroutines.flow.Flow

interface RankingRepository {
    // Using Flow in case rankings could update in real-time, or for a one-shot load.
    fun getPetRankings(): Flow<List<RankedPet>>
}