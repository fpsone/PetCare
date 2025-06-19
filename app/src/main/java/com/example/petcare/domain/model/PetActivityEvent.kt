package com.example.petcare.domain.model

data class PetActivityEvent(
    val id: String,
    val timestamp: Long, // Epoch milliseconds
    val type: ActivityType,
    val description: String,
    val iconName: String // Material Symbol name or a custom identifier
)

enum class ActivityType {
    WALK_START,
    WALK_END,
    MEAL,
    PLAY,
    SLEEP_START,
    SLEEP_END,
    TREAT,
    VET_VISIT,
    LOCATION_UPDATE // Generic location update if not tied to a specific activity
}