package com.example.petcare.presentation.mvi

import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.model.WearableData

object HomeContract {

    data class UiState(
        val isLoadingPetProfile: Boolean = true,
        val isLoadingWearableData: Boolean = true,
        val isLoadingActivityHistory: Boolean = true,
        val petProfile: PetProfile? = null,
        val wearableData: WearableData? = null,
        val activityHistory: List<PetActivityEvent> = emptyList(),
        val error: String? = null // General error message for the screen
    ) {
        val overallLoading: Boolean
            get() = isLoadingPetProfile || isLoadingWearableData || isLoadingActivityHistory
    }

    sealed interface Event {
        object LoadData : Event // Could be triggered on init or refresh
        object RefreshData : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        // object NavigateToSettings // Example, if needed
    }
}