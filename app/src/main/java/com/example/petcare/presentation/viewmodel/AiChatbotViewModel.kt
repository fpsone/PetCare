package com.example.petcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.domain.model.ChatMessage
import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.usecase.chat.SendMessageUseCase
import com.example.petcare.domain.usecase.pet.GetPetProfileUseCase
import com.example.petcare.presentation.mvi.AiChatbotContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatbotViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getPetProfileUseCase: GetPetProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatbotContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AiChatbotContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var petProfile: PetProfile? = null

    init {
        loadPetProfile()
    }

    private fun loadPetProfile() {
        viewModelScope.launch {
            getPetProfileUseCase().first()?.let {
                petProfile = it
                _uiState.update { state -> state.copy(isPetProfileLoaded = true) }
            } ?: run {
                _uiState.update { state -> state.copy(error = "Pet profile not found. Please set up your pet's profile first.", isPetProfileLoaded = false) }
                _effect.emit(AiChatbotContract.Effect.ShowSnackbar("Pet profile needed for AI Chat."))
            }
        }
    }

    fun sendEvent(event: AiChatbotContract.Event) {
        when (event) {
            is AiChatbotContract.Event.OnMessageInputChange -> {
                _uiState.update { it.copy(currentMessageInput = event.text) }
            }
            is AiChatbotContract.Event.OnSendMessageClick -> {
                sendMessage(_uiState.value.currentMessageInput)
            }
            is AiChatbotContract.Event.OnRecommendedQuestionClick -> {
                _uiState.update { it.copy(currentMessageInput = event.question) } // Optionally send immediately or just fill input
                sendMessage(event.question)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        if (messageText.isBlank() || petProfile == null) {
            if(petProfile == null) viewModelScope.launch { _effect.emit(AiChatbotContract.Effect.ShowSnackbar("Pet profile not loaded.")) }
            return
        }

        val userMessage = ChatMessage(UUID.randomUUID().toString(), messageText, System.currentTimeMillis(), true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                currentMessageInput = "",
                isLoadingResponse = true,
                error = null
            )
        }

        viewModelScope.launch {
            val currentPet = petProfile!! // Safe due to check above
            sendMessageUseCase(
                userMessage = messageText,
                petName = currentPet.name,
                petBreed = currentPet.breed,
                petAge = currentPet.age,
                petGender = currentPet.gender.name,
                petWeight = currentPet.weight
            ).onSuccess { aiResponseText ->
                val aiMessage = ChatMessage(UUID.randomUUID().toString(), aiResponseText, System.currentTimeMillis(), false)
                _uiState.update { it.copy(messages = it.messages + aiMessage, isLoadingResponse = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingResponse = false, error = error.localizedMessage ?: "Failed to get response") }
                _effect.emit(AiChatbotContract.Effect.ShowSnackbar("Error: ${error.localizedMessage}"))
            }
        }
    }
}