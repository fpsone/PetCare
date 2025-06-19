package com.example.petcare.presentation.mvi

import com.example.petcare.domain.model.ChatMessage

object AiChatbotContract {

    data class UiState(
        val messages: List<ChatMessage> = emptyList(),
        val currentMessageInput: String = "",
        val isLoadingResponse: Boolean = false,
        val error: String? = null,
        val recommendedQuestions: List<String> = listOf(
            "What are common health issues for my pet's breed?",
            "How much exercise does my pet need daily?",
            "What's a good diet plan for my pet?",
            "How to train my pet to sit?"
        ),
        val isPetProfileLoaded: Boolean = false // To ensure pet profile is loaded before enabling chat
    )

    sealed interface Event {
        data class OnMessageInputChange(val text: String) : Event
        object OnSendMessageClick : Event
        data class OnRecommendedQuestionClick(val question: String) : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
    }
}