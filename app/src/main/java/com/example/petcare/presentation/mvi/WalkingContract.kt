package com.example.petcare.presentation.mvi

import com.example.petcare.domain.model.PetLocation
import com.google.android.gms.maps.model.LatLng

object WalkingContract {

    data class UiState(
        val isLoadingMapStyle: Boolean = true,
        val currentPetLocation: PetLocation? = null,
        val locationHistory: List<PetLocation> = emptyList(), // For drawing the path
        val mapStyleJson: String? = null,
        val searchQuery: String = "",
        val distanceWalkedFormatted: String = "0.00 km",
        val walkDurationFormatted: String = "00:00", // MM:SS
        val isPetWalking: Boolean = false, // To control when to record distance/duration
        val error: String? = null
    )

    sealed interface Event {
        object LoadMapStyle : Event
        data class OnSearchQueryChanged(val query: String) : Event
        object OnSearchSubmit : Event
        // If we want to manually start/stop a walk session for stat tracking:
        // object ToggleWalkSession : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        data class AnimateMapToLocation(val location: LatLng) : Effect
        // object ClearSearchFocus : Effect // If needed after search submit
    }
}