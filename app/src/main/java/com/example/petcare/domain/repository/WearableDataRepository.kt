package com.example.petcare.domain.repository

import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.model.WearableData
import kotlinx.coroutines.flow.Flow

interface WearableDataRepository {
    fun getWearableDataStream(): Flow<WearableData>
    fun getActivityHistoryStream(): Flow<List<PetActivityEvent>>
    // Potentially add methods to fetch specific data points or ranges
}