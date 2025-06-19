package com.example.petcare.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isFromUser: Boolean
)