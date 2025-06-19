package com.example.petcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.domain.usecase.pet.GetPetProfileUseCase
import com.example.petcare.domain.usecase.wearable.GetActivityHistoryStreamUseCase
import com.example.petcare.domain.usecase.wearable.GetWearableDataStreamUseCase
import com.example.petcare.presentation.mvi.HomeContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPetProfileUseCase: GetPetProfileUseCase,
    private val getWearableDataStreamUseCase: GetWearableDataStreamUseCase,
    private val getActivityHistoryStreamUseCase: GetActivityHistoryStreamUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.Effect>()
    val effect = _effect.asSharedFlow()

    init {
        sendEvent(HomeContract.Event.LoadData)
    }

    fun sendEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.LoadData -> loadAllData()
            is HomeContract.Event.RefreshData -> loadAllData(isRefresh = true)
        }
    }

    private fun loadAllData(isRefresh: Boolean = false) {
        if (isRefresh) {
            _uiState.update { it.copy(isLoadingPetProfile = true, isLoadingWearableData = true, isLoadingActivityHistory = true, error = null) }
        }

        // Load Pet Profile
        getPetProfileUseCase()
            .onEach { profile ->
                _uiState.update { it.copy(petProfile = profile, isLoadingPetProfile = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load pet profile: ${e.localizedMessage}", isLoadingPetProfile = false) }
                _effect.emit(HomeContract.Effect.ShowSnackbar("Error loading profile."))
            }
            .launchIn(viewModelScope)

        // Load Wearable Data Stream
        getWearableDataStreamUseCase()
            .onEach { wearableData ->
                _uiState.update { it.copy(wearableData = wearableData, isLoadingWearableData = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load wearable data: ${e.localizedMessage}", isLoadingWearableData = false) }
            }
            .launchIn(viewModelScope)

        // Load Activity History Stream
        getActivityHistoryStreamUseCase()
            .onEach { history ->
                _uiState.update { it.copy(activityHistory = history, isLoadingActivityHistory = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load activity history: ${e.localizedMessage}", isLoadingActivityHistory = false) }
            }
            .launchIn(viewModelScope)
    }
}