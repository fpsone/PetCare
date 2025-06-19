package com.example.petcare.data.datasource

import com.example.petcare.domain.model.ActivityType
import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.model.PetLocation
import com.example.petcare.domain.model.PetStatus
import com.example.petcare.domain.model.WearableData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockWearableRemoteDataSource @Inject constructor() {

    private var currentBatteryLevel = 100
    private var lastStatus: PetStatus = PetStatus.IDLE
    private var lastLocation = PetLocation(34.0522, -118.2437) // Initial location (e.g., Los Angeles)
    private var stepsToday = Random.nextInt(500, 2000)

    private val activityHistory = mutableListOf<PetActivityEvent>()

    companion object {
        private const val DATA_EMISSION_INTERVAL_MS = 5000L // Emit data every 5 seconds
        private const val ACTIVITY_GENERATION_INTERVAL_MS = 30000L // Generate a new activity event less frequently
        private const val NIGHT_START_HOUR = 22 // 10 PM
        private const val NIGHT_END_HOUR = 6   // 6 AM
        private const val LOCATION_DRIFT_IDLE = 0.0001
        private const val LOCATION_DRIFT_ACTIVE = 0.001
        private const val LOCATION_DRIFT_RUNNING = 0.002
    }

    fun getMockWearableDataStream(): Flow<WearableData> = channelFlow {
        while (isActive) {
            val timestamp = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            // Simulate battery drain
            if (currentBatteryLevel > 0 && Random.nextInt(100) < 5) { // 5% chance to drop battery every emission
                currentBatteryLevel -= 1
            }

            // Simulate pet status with time-based intelligence
            val newStatus = if (currentHour >= NIGHT_START_HOUR || currentHour < NIGHT_END_HOUR) {
                if (Random.nextDouble() < 0.8) PetStatus.SLEEPING else PetStatus.IDLE // 80% chance of sleeping at night
            } else {
                // Daytime activities
                when (Random.nextInt(10)) {
                    0, 1 -> PetStatus.WALKING
                    2 -> PetStatus.RUNNING
                    3, 4 -> PetStatus.PLAYING
                    5 -> PetStatus.EATING
                    else -> PetStatus.IDLE
                }
            }

            // Simulate location drift
            val driftFactor = when (newStatus) {
                PetStatus.WALKING -> LOCATION_DRIFT_ACTIVE
                PetStatus.RUNNING -> LOCATION_DRIFT_RUNNING
                else -> LOCATION_DRIFT_IDLE
            }
            val latDrift = (Random.nextDouble() - 0.5) * 2 * driftFactor // -1 to 1 * driftFactor
            val lonDrift = (Random.nextDouble() - 0.5) * 2 * driftFactor

            lastLocation = PetLocation(
                latitude = (lastLocation.latitude + latDrift).coerceIn(-90.0, 90.0),
                longitude = (lastLocation.longitude + lonDrift).coerceIn(-180.0, 180.0)
            )

            // Simulate steps
            if (newStatus == PetStatus.WALKING || newStatus == PetStatus.RUNNING || newStatus == PetStatus.PLAYING) {
                stepsToday += Random.nextInt(10, 50)
            }

            // Add to activity history if status changes significantly or a notable event occurs
            if (newStatus != lastStatus || (newStatus == PetStatus.EATING && lastStatus != PetStatus.EATING)) {
                val activityType = when (newStatus) {
                    PetStatus.SLEEPING -> if (lastStatus != PetStatus.SLEEPING) ActivityType.SLEEP_START else null
                    PetStatus.WALKING -> if (lastStatus != PetStatus.WALKING && lastStatus != PetStatus.RUNNING) ActivityType.WALK_START else null
                    PetStatus.RUNNING -> if (lastStatus != PetStatus.RUNNING && lastStatus != PetStatus.WALKING) ActivityType.WALK_START else null // Could be more specific
                    PetStatus.EATING -> ActivityType.MEAL
                    PetStatus.PLAYING -> ActivityType.PLAY
                    else -> if (lastStatus == PetStatus.SLEEPING && newStatus != PetStatus.SLEEPING) ActivityType.SLEEP_END else null
                }
                activityType?.let {
                    val description = when(it) {
                        ActivityType.SLEEP_START -> "Fell asleep"
                        ActivityType.SLEEP_END -> "Woke up"
                        ActivityType.WALK_START -> if(newStatus == PetStatus.RUNNING) "Started running" else "Started a walk"
                        ActivityType.MEAL -> "Had a meal"
                        ActivityType.PLAY -> "Started playing"
                        else -> newStatus.displayName
                    }
                    val icon = when(it) {
                        ActivityType.SLEEP_START, ActivityType.SLEEP_END -> "bed" // Material Symbol name
                        ActivityType.WALK_START, ActivityType.WALK_END -> "directions_walk"
                        ActivityType.MEAL -> "restaurant"
                        ActivityType.PLAY -> "sports_esports" // Example
                        else -> "pets"
                    }
                    synchronized(activityHistory) {
                        if (activityHistory.size > 100) activityHistory.removeAt(0) // Keep history size manageable
                        activityHistory.add(
                            PetActivityEvent(
                                id = UUID.randomUUID().toString(),
                                timestamp = timestamp,
                                type = it,
                                description = description,
                                iconName = icon
                            )
                        )
                    }
                }
            }
            lastStatus = newStatus

            val wearableData = WearableData(
                timestamp = timestamp,
                batteryLevel = currentBatteryLevel,
                petStatus = newStatus,
                location = lastLocation,
                stepsToday = stepsToday
            )
            send(wearableData)
            delay(DATA_EMISSION_INTERVAL_MS)
        }
    }

    fun getMockActivityHistoryStream(): Flow<List<PetActivityEvent>> = channelFlow {
        var lastSentSize = 0
        while (isActive) {
            val currentHistory: List<PetActivityEvent>
            synchronized(activityHistory) {
                currentHistory = ArrayList(activityHistory) // Create a copy to avoid concurrent modification
            }
            if (currentHistory.size != lastSentSize) {
                send(currentHistory.sortedByDescending { it.timestamp })
                lastSentSize = currentHistory.size
            }
            // This flow will re-emit the list if it changes.
            // The check `currentHistory.size != lastSentSize` ensures we only emit when there's a new event.
            // The actual generation of activity events happens within `getMockWearableDataStream`.
            delay(ACTIVITY_GENERATION_INTERVAL_MS / 3) // Check for history updates more frequently than generation
        }
    }
}