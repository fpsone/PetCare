package com.example.petcare.domain.model

enum class PetStatus(val displayName: String) {
    SLEEPING("Sleeping"),
    WALKING("Walking"),
    RUNNING("Running"),
    PLAYING("Playing"),
    EATING("Eating"),
    IDLE("Idle")
}