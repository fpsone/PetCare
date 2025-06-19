package com.example.petcare.domain.model

data class WearableData(
    val timestamp: Long, // Epoch milliseconds
    val batteryLevel: Int, // Percentage 0-100
    val petStatus: PetStatus,
    val location: PetLocation,
    val stepsToday: Int = 0 // Example of an aggregated daily stat
)