package com.example.petcare.domain.model

data class RankedPet(
    val id: String,
    val name: String,
    val avatarUrl: String?, // URL or local resource identifier
    val score: Int, // e.g., activity points
    val rank: Int
)