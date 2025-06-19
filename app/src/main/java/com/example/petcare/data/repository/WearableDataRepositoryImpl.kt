package com.example.petcare.data.repository

import com.example.petcare.data.datasource.MockWearableRemoteDataSource
import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.model.WearableData
import com.example.petcare.domain.repository.WearableDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableDataRepositoryImpl @Inject constructor(
    private val remoteDataSource: MockWearableRemoteDataSource
) : WearableDataRepository {

    override fun getWearableDataStream(): Flow<WearableData> {
        return remoteDataSource.getMockWearableDataStream()
    }

    override fun getActivityHistoryStream(): Flow<List<PetActivityEvent>> {
        return remoteDataSource.getMockActivityHistoryStream()
    }

    // In a real app, you might have additional methods here to handle:
    // - Caching of wearable data
    // - Error handling and retries
    // - Data transformations or filtering
}