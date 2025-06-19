package com.example.petcare.domain.usecase.wearable

import com.example.petcare.domain.model.WearableData
import com.example.petcare.domain.repository.WearableDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWearableDataStreamUseCase @Inject constructor(
    private val repository: WearableDataRepository
) {
    operator fun invoke(): Flow<WearableData> = repository.getWearableDataStream()
}