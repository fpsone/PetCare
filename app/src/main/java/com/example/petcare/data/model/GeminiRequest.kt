package com.example.petcare.data.model

import com.google.gson.annotations.SerializedName

// Simplified request structure
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(val text: String)