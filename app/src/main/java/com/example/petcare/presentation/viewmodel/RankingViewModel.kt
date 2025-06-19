package com.example.petcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.domain.usecase.ranking.GetPetRankingsUseCase
import com.example.petcare.presentation.mvi.RankingContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val getPetRankingsUseCase: GetPetRankingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RankingContract.Effect>()
    val effect = _effect.asSharedFlow()

    init {
        sendEvent(RankingContract.Event.LoadRankings)
    }

    fun sendEvent(event: RankingContract.Event) {
        when (event) {
            is RankingContract.Event.LoadRankings -> loadRankings(isRefresh = false)
            is RankingContract.Event.RefreshRankings -> loadRankings(isRefresh = true)
        }
    }

    private fun loadRankings(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = if (isRefresh) null else it.error) }
            getPetRankingsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "An unexpected error occurred"
                        )
                    }
                    _effect.emit(RankingContract.Effect.ShowSnackbar("Error loading rankings: ${e.localizedMessage}"))
                }
                .collect { rankings ->
                    _uiState.update { it.copy(isLoading = false, rankings = rankings, error = null) }
                }
        }
    }
}