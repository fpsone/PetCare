package com.example.petcare.presentation.mvi

import com.example.petcare.domain.model.PetGender
import com.example.petcare.domain.model.PetProfile
import java.util.UUID

object SettingsContract {

    data class UiState(
        val isLoading: Boolean = true,
        val isEditing: Boolean = false, // True if a profile exists and is being edited
        val petName: String = "",
        val petBreed: String = "",
        val petAge: Int = 0, // Will be calculated from DOB
        val petGender: PetGender = PetGender.UNKNOWN,
        val petWeight: String = "", // String to handle user input, convert to Double on save
        val dateOfBirthTimestamp: Long? = null, // Nullable until selected
        val hasChanges: Boolean = false,
        val error: String? = null,
        // For generating a random profile if none exists
        val isFirstTimeSetup: Boolean = false
    )

    sealed interface Event {
        object LoadPetProfile : Event
        data class OnNameChanged(val name: String) : Event
        data class OnBreedChanged(val breed: String) : Event
        data class OnGenderSelected(val gender: PetGender) : Event
        data class OnWeightChanged(val weight: String) : Event
        data class OnDateOfBirthSelected(val timestamp: Long) : Event
        object OnSaveClick : Event
        object OnEditClick : Event // If we want an explicit edit mode toggle
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        object NavigateToHome : Effect
        // Potentially: object OpenDatePickerDialog
    }
}