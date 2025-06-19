package com.example.petcare.data.datasource

import com.example.petcare.domain.model.RankedPet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockRankingDataSource @Inject constructor() {

    private val petNames = listOf(
        "Buddy", "Lucy", "Max", "Bella", "Charlie", "Daisy", "Cooper", "Luna", "Milo", "Sadie",
        "Rocky", "Zoe", "Bear", "Lola", "Duke", "Ruby", "Tucker", "Rosie", "Oliver", "Piper"
    )

    // Simulate fetching data with a delay
    fun getRankings(): Flow<List<RankedPet>> = flow {
        delay(1500) // Simulate network delay

        // Simulate potential error
        // if (Random.nextBoolean()) {
        //     throw Exception("Failed to load rankings. Please try again.")
        // }

        val rankings = (1..Random.nextInt(5, 15)).mapIndexed { index, _ ->
            RankedPet(
                id = UUID.randomUUID().toString(),
                name = petNames.random(),
                // In a real app, avatarUrl would come from a pet's profile or a default
                avatarUrl = "https://picsum.photos/seed/${Random.nextInt(1000)}/200", // Placeholder image
                score = Random.nextInt(5000, 15000),
                rank = index + 1
            )
        }.sortedByDescending { it.score }
            .mapIndexed { index, pet -> pet.copy(rank = index + 1) } // Re-assign rank after sorting by score

        emit(rankings.take(10)) // Take top 10
    }
}