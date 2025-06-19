package com.example.petcare.domain.usecase.wearable

import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.repository.WearableDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityHistoryStreamUseCase @Inject constructor(
    private val repository: WearableDataRepository
) {
    operator fun invoke(): Flow<List<PetActivityEvent>> = repository.getActivityHistoryStream()
}