package com.example.petcare.domain.usecase.chat

import com.example.petcare.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        userMessage: String,
        petName: String,
        petBreed: String,
        petAge: Int,
        petGender: String,
        petWeight: Double
    ): Result<String> {
        if (userMessage.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be empty."))
        return chatRepository.sendMessage(userMessage, petName, petBreed, petAge, petGender, petWeight)
    }
}