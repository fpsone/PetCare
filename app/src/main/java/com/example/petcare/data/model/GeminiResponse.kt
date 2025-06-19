package com.example.petcare.data.model

// Simplified response structure - assuming the response contains generated text directly
// A more realistic Gemini response would be more complex, potentially with candidates, safety ratings, etc.
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val promptFeedback: PromptFeedback?
)

data class Candidate(
    val content: Content,
    // other fields like finishReason, safetyRatings etc.
)

data class PromptFeedback(
    val safetyRatings: List<SafetyRating>?
)
data class SafetyRating(val category: String, val probability: String)