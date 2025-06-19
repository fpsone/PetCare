package com.example.petcare.domain.model

data class PetProfile(
    val id: String = "default_pet", // A unique ID for the pet
    val name: String,
    val breed: String,
    val age: Int, // in years
    val gender: PetGender,
    val weight: Double, // in kg
    val dateOfBirthTimestamp: Long, // Store as Long (epoch millis) for easier handling
    val avatarUrl: String? = null // Optional: URL for pet's avatar
)

enum class PetGender { MALE, FEMALE, UNKNOWN }