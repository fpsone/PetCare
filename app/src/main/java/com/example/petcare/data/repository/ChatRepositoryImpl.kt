package com.example.petcare.data.repository

import com.example.petcare.data.datasource.GeminiApiService
import com.example.petcare.data.model.Content
import com.example.petcare.data.model.GeminiRequest
import com.example.petcare.data.model.Part
import com.example.petcare.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService
) : ChatRepository {

    override suspend fun sendMessage(
        userMessage: String,
        petName: String,
        petBreed: String,
        petAge: Int,
        petGender: String,
        petWeight: Double
    ): Result<String> {
        val systemPrompt = """
        You are PetPal, an expert and friendly AI pet care assistant. 
        You are advising the owner of $petName, a $petAge-year-old $petGender $petBreed weighing $petWeight kg. 
        Your advice must be safe, practical, and tailored to this specific pet. 
        Maintain a warm, encouraging, and slightly playful tone.
        User's question: $userMessage
        """.trimIndent()

        // For Gemini, the "system prompt" often goes as the first part of the user's content,
        // or you might structure it differently based on the specific Gemini model and its API.
        // Here, we'll prepend it to the user's message for simplicity with the gemini-pro model.
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = systemPrompt)))))

        return try {
            val response = geminiApiService.generateContent(request)
            val aiResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Sorry, I couldn't generate a response."
            Result.success(aiResponseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}