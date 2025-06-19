package com.example.petcare.domain.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(userMessage: String, petName: String, petBreed: String, petAge: Int, petGender: String, petWeight: Double): Result<String>
    // We might also want a method to get chat history if we persist it
    // fun getChatHistory(): Flow<List<ChatMessage>>
}