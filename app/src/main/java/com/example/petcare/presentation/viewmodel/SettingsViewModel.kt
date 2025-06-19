package com.example.petcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.domain.model.PetGender
import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.usecase.pet.GetPetProfileUseCase
import com.example.petcare.domain.usecase.pet.SavePetProfileUseCase
import com.example.petcare.presentation.mvi.SettingsContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getPetProfileUseCase: GetPetProfileUseCase,
    private val savePetProfileUseCase: SavePetProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var originalProfile: PetProfile? = null

    init {
        sendEvent(SettingsContract.Event.LoadPetProfile)
    }

    fun sendEvent(event: SettingsContract.Event) {
        when (event) {
            is SettingsContract.Event.LoadPetProfile -> loadProfile()
            is SettingsContract.Event.OnNameChanged -> updateState { copy(petName = event.name, hasChanges = true) }
            is SettingsContract.Event.OnBreedChanged -> updateState { copy(petBreed = event.breed, hasChanges = true) }
            is SettingsContract.Event.OnGenderSelected -> updateState { copy(petGender = event.gender, hasChanges = true) }
            is SettingsContract.Event.OnWeightChanged -> updateState { copy(petWeight = event.weight, hasChanges = true) }
            is SettingsContract.Event.OnDateOfBirthSelected -> {
                val age = calculateAge(event.timestamp)
                updateState { copy(dateOfBirthTimestamp = event.timestamp, petAge = age, hasChanges = true) }
            }
            is SettingsContract.Event.OnSaveClick -> saveProfile()
            is SettingsContract.Event.OnEditClick -> updateState { copy(isEditing = true) } // Or toggle if needed
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val profile = getPetProfileUseCase().first()
            if (profile != null) {
                originalProfile = profile
                updateState {
                    copy(
                        isLoading = false,
                        isEditing = true, // Existing profile, so we are in "edit" mode
                        petName = profile.name,
                        petBreed = profile.breed,
                        petAge = profile.age,
                        petGender = profile.gender,
                        petWeight = profile.weight.toString(),
                        dateOfBirthTimestamp = profile.dateOfBirthTimestamp,
                        hasChanges = false,
                        isFirstTimeSetup = false
                    )
                }
            } else {
                // No profile exists, prepare for first-time setup or generate random
                // For now, let's assume user will input. Random generation can be a separate event if needed.
                originalProfile = null
                updateState {
                    copy(
                        isLoading = false,
                        isEditing = false, // No profile, so creating new
                        isFirstTimeSetup = true,
                        hasChanges = false // Initially no changes for a new profile form
                    )
                }
            }
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val weightDouble = currentState.petWeight.toDoubleOrNull()

            if (currentState.petName.isBlank() || currentState.petBreed.isBlank() || weightDouble == null || weightDouble <= 0 || currentState.dateOfBirthTimestamp == null) {
                updateState { copy(error = "Please fill all fields correctly.") }
                _effect.emit(SettingsContract.Effect.ShowSnackbar("Please fill all fields correctly."))
                return@launch
            }

            val petProfile = PetProfile(
                id = originalProfile?.id ?: UUID.randomUUID().toString(),
                name = currentState.petName,
                breed = currentState.petBreed,
                age = currentState.petAge,
                gender = currentState.petGender,
                weight = weightDouble,
                dateOfBirthTimestamp = currentState.dateOfBirthTimestamp
            )

            savePetProfileUseCase(petProfile)
            originalProfile = petProfile // Update original profile after save
            updateState { copy(hasChanges = false, error = null, isEditing = true, isFirstTimeSetup = false) }
            _effect.emit(SettingsContract.Effect.ShowSnackbar("Profile for ${petProfile.name} updated!"))
            if (currentState.isFirstTimeSetup) { // If it was the first time, navigate to home
                _effect.emit(SettingsContract.Effect.NavigateToHome)
            }
        }
    }

    private fun calculateAge(birthTimestamp: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age.coerceAtLeast(0)
    }

    private fun updateState(updateAction: SettingsContract.UiState.() -> SettingsContract.UiState) {
        _uiState.update(updateAction)
    }
}