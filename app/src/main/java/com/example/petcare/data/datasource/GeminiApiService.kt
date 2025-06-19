package com.example.petcare.data.datasource

import com.example.petcare.BuildConfig
import com.example.petcare.data.model.GeminiRequest
import com.example.petcare.data.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/gemini-pro:generateContent") // Example endpoint for gemini-pro
    suspend fun generateContent(
        @Body request: GeminiRequest,
        @Query("key") apiKey: String = BuildConfig.GEMINI_API_KEY
    ): GeminiResponse
}