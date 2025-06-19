package com.example.petcare.presentation.mvi

import com.example.petcare.domain.model.RankedPet

object RankingContract {

    data class UiState(
        val isLoading: Boolean = true,
        val rankings: List<RankedPet> = emptyList(),
        val error: String? = null
    )

    sealed interface Event {
        object LoadRankings : Event
        object RefreshRankings : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
    }
}