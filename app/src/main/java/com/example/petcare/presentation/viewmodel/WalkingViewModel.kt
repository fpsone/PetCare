package com.example.petcare.presentation.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.R
import com.example.petcare.domain.model.PetLocation
import com.example.petcare.domain.model.PetStatus
import com.example.petcare.domain.usecase.wearable.GetWearableDataStreamUseCase
import com.example.petcare.presentation.mvi.WalkingContract
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WalkingViewModel @Inject constructor(
    application: Application, // For accessing raw resources
    private val getWearableDataStreamUseCase: GetWearableDataStreamUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WalkingContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WalkingContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var walkStartTime: Long? = null
    private var totalDistanceMeters: Float = 0f
    private var lastTrackedLocation: PetLocation? = null
    private var durationUpdateJob: Job? = null

    init {
        sendEvent(WalkingContract.Event.LoadMapStyle)
        observeWearableData()
    }

    fun sendEvent(event: WalkingContract.Event) {
        when (event) {
            is WalkingContract.Event.LoadMapStyle -> loadMapStyle()
            is WalkingContract.Event.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is WalkingContract.Event.OnSearchSubmit -> {
                // Handle search submission (e.g., geocode query and move map)
                // For now, just a placeholder
                viewModelScope.launch {
                    _effect.emit(WalkingContract.Effect.ShowSnackbar("Search for: ${uiState.value.searchQuery} (not implemented)"))
                }
            }
        }
    }

    private fun loadMapStyle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMapStyle = true) }
            try {
                val styleJson = readRawResource(R.raw.map_style)
                _uiState.update { it.copy(mapStyleJson = styleJson, isLoadingMapStyle = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load map style", isLoadingMapStyle = false) }
                _effect.emit(WalkingContract.Effect.ShowSnackbar("Error loading map style."))
            }
        }
    }

    private suspend fun readRawResource(resourceId: Int): String = withContext(Dispatchers.IO) {
        val inputStream = getApplication<Application>().resources.openRawResource(resourceId)
        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
    }

    private fun observeWearableData() {
        getWearableDataStreamUseCase()
            .onEach { wearableData ->
                val newLocation = wearableData.location
                val isCurrentlyWalking = wearableData.petStatus == PetStatus.WALKING || wearableData.petStatus == PetStatus.RUNNING

                _uiState.update { currentState ->
                    val updatedHistory = if (isCurrentlyWalking) currentState.locationHistory + newLocation else currentState.locationHistory
                    currentState.copy(
                        currentPetLocation = newLocation,
                        locationHistory = updatedHistory.takeLast(100), // Keep history manageable
                        isPetWalking = isCurrentlyWalking
                    )
                }

                if (isCurrentlyWalking) {
                    if (walkStartTime == null) { // Walk just started
                        walkStartTime = System.currentTimeMillis()
                        lastTrackedLocation = newLocation
                        startDurationUpdates()
                    }
                    lastTrackedLocation?.let { prevLoc ->
                        val results = FloatArray(1)
                        Location.distanceBetween(prevLoc.latitude, prevLoc.longitude, newLocation.latitude, newLocation.longitude, results)
                        totalDistanceMeters += results[0]
                    }
                    lastTrackedLocation = newLocation
                    _uiState.update { it.copy(distanceWalkedFormatted = String.format("%.2f km", totalDistanceMeters / 1000f)) }
                } else { // Pet stopped walking
                    walkStartTime = null // Reset start time
                    durationUpdateJob?.cancel()
                    // totalDistanceMeters and lastTrackedLocation could be reset here if a "walk session" is strictly defined
                }

                // Animate map to new location only if it's significantly different or first location
                if (_uiState.value.locationHistory.size == 1 || wearableData.petStatus == PetStatus.WALKING || wearableData.petStatus == PetStatus.RUNNING) {
                     _effect.emit(WalkingContract.Effect.AnimateMapToLocation(LatLng(newLocation.latitude, newLocation.longitude)))
                }
            }
            .catch { e -> _uiState.update { it.copy(error = "Failed to get location updates: ${e.message}") } }
            .launchIn(viewModelScope)
    }

    private fun startDurationUpdates() {
        durationUpdateJob?.cancel()
        durationUpdateJob = viewModelScope.launch {
            while (uiState.value.isPetWalking && walkStartTime != null) {
                val elapsedMillis = System.currentTimeMillis() - (walkStartTime ?: System.currentTimeMillis())
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                _uiState.update { it.copy(walkDurationFormatted = String.format("%02d:%02d", minutes, seconds)) }
                delay(1000) // Update every second
            }
        }
    }
}