package com.example.petcare.presentation.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
   import androidx.compose.material.icons.filled.Pets // Explicit import (try this if the problem persists)
import androidx.compose.material.icons.filled.ErrorOutline // Default pet icon
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter // For Play
import androidx.compose.material.icons.rounded.Healing // For Vet
import androidx.compose.material.icons.rounded.Hotel // For Sleep End
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt // Idle
import androidx.compose.material.icons.rounded.SportsEsports // Play
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.petcare.R // Assuming you have a default pet avatar in drawable
import com.example.petcare.domain.model.ActivityType
import com.example.petcare.domain.model.PetActivityEvent
import com.example.petcare.domain.model.PetProfile
import com.example.petcare.domain.model.PetStatus
import com.example.petcare.domain.model.WearableData
import com.example.petcare.presentation.mvi.HomeContract
import com.example.petcare.presentation.ui.components.ShimmerPlaceholder
import com.example.petcare.presentation.ui.theme.BatteryGreen
import com.example.petcare.presentation.ui.theme.BatteryOrange
import com.example.petcare.presentation.ui.theme.BatteryRed
import com.example.petcare.presentation.ui.theme.PetCareTheme
import com.example.petcare.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PetCare Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendEvent(HomeContract.Event.RefreshData) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoadingPetProfile && uiState.petProfile != null || uiState.isLoadingPetProfile,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    HeroInfoCard(
                        petProfile = uiState.petProfile,
                        wearableData = uiState.wearableData,
                        isLoading = uiState.isLoadingPetProfile || uiState.isLoadingWearableData
                    )
                }
            }

            item {
                Text(
                    "Today's Activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            if (uiState.isLoadingActivityHistory && uiState.activityHistory.isEmpty()) {
                items(3) { // Shimmer placeholders for timeline
                    ShimmerPlaceholder(height = 80.dp, shapeRadius = 12.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (!uiState.isLoadingActivityHistory && uiState.activityHistory.isEmpty()) {
                item {
                    EmptyStateView("No activity recorded yet!")
                }
            } else {
                items(uiState.activityHistory, key = { it.id }) { event ->
                    ActivityTimelineItem(event = event, modifier = Modifier.animateItemPlacement(tween(durationMillis = 300)))
                }
            }

            if (uiState.error != null && !uiState.overallLoading) {
                item {
                    ErrorStateView(
                        message = uiState.error ?: "An unknown error occurred.",
                        onRetry = { viewModel.sendEvent(HomeContract.Event.RefreshData) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) } // Bottom padding
        }
    }
}

@Composable
fun HeroInfoCard(petProfile: PetProfile?, wearableData: WearableData?, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            Column(modifier = Modifier.padding(16.dp)) {
                ShimmerPlaceholder(height = 30.dp, shapeRadius = 6.dp)
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(height = 20.dp, shapeRadius = 4.dp)
                Spacer(modifier = Modifier.height(12.dp))
                ShimmerPlaceholder(height = 20.dp, shapeRadius = 4.dp)
            }
        } else if (petProfile != null) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(petProfile.avatarUrl ?: R.drawable.ic_default_pet_avatar) // Provide a default avatar
                            .crossfade(true)
                            .build(),
                        error = painterResource(id = R.drawable.ic_default_pet_avatar) // Fallback for error
                    ),
                    contentDescription = "${petProfile.name}'s avatar",
                    modifier = Modifier.size(80.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(petProfile.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PetStatusIcon(status = wearableData?.petStatus ?: PetStatus.IDLE)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            wearableData?.petStatus?.displayName ?: "Status Unknown",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    wearableData?.let {
                        BatteryIndicator(batteryLevel = it.batteryLevel)
                    }
                }
            }
        } else {
            // Could be an error state specific to pet profile loading
            Text("Pet profile not available.", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun PetStatusIcon(status: PetStatus) {
    val icon = when (status) {
        PetStatus.SLEEPING -> Icons.Rounded.Bed
        PetStatus.WALKING -> Icons.Rounded.DirectionsWalk
        PetStatus.RUNNING -> Icons.Rounded.DirectionsWalk // Could use a different one
        PetStatus.PLAYING -> Icons.Rounded.SportsEsports
        PetStatus.EATING -> Icons.Rounded.Fastfood
        PetStatus.IDLE -> Icons.Rounded.SentimentSatisfiedAlt
    }
    Icon(icon, contentDescription = status.displayName, tint = MaterialTheme.colorScheme.primary)
}

@Composable
fun BatteryIndicator(batteryLevel: Int) {
    val batteryColor by animateColorAsState(
        targetValue = when {
            batteryLevel > 50 -> BatteryGreen
            batteryLevel > 20 -> BatteryOrange
            else -> BatteryRed
        },
        animationSpec = tween(durationMillis = 500), label = "batteryColorAnimation"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.BatteryFull, contentDescription = "Battery", tint = batteryColor)
        Spacer(modifier = Modifier.width(4.dp))
        Text("$batteryLevel%", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActivityTimelineItem(event: PetActivityEvent, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 8.dp)) {
        TimelineMarker(icon = getActivityIcon(event.type))
        Spacer(modifier = Modifier.width(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(event.description, style = MaterialTheme.typography.titleMedium)
                Text(
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(event.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimelineMarker(icon: ImageVector) {
    val primaryColor = MaterialTheme.colorScheme.primary // Resolve color in Composable scope
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = "Activity Icon",
            tint = primaryColor, // Use the resolved color
            modifier = Modifier.size(28.dp)
        )
        Canvas(modifier = Modifier.height(50.dp).width(2.dp)) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = primaryColor, // Use the resolved color from the Composable scope
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 2.dp.toPx(),
                pathEffect = pathEffect
            )
        }
    }
}

fun getActivityIcon(type: ActivityType): ImageVector {
    // Accessing Icons here is fine as it's not in a Composable context requiring CompositionLocal resolution
    return when (type) {
        ActivityType.WALK_START, ActivityType.WALK_END -> Icons.Rounded.DirectionsWalk
        ActivityType.MEAL -> Icons.Rounded.Fastfood
        ActivityType.PLAY -> Icons.Rounded.FitnessCenter // Or SportsEsports
        ActivityType.SLEEP_START -> Icons.Rounded.Bed
        ActivityType.SLEEP_END -> Icons.Rounded.Hotel // Or a sun icon
        ActivityType.TREAT -> Icons.Rounded.Pets // Placeholder
        ActivityType.VET_VISIT -> Icons.Rounded.Healing
        ActivityType.LOCATION_UPDATE -> Icons.Rounded.LocationOn
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Pets, // Use directly imported 'Pets' from androidx.compose.material.icons.filled.Pets
            contentDescription = "Empty",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetCareTheme {
        // Cannot fully preview with ViewModel state here easily
        // HomeScreen(navController = rememberNavController())
        // Preview individual components instead
        HeroInfoCard(
            petProfile = PetProfile("1", "Buddy", "Labrador", 3, com.example.petcare.domain.model.PetGender.MALE, 25.0, Date().time),
            wearableData = WearableData(Date().time, 75, PetStatus.WALKING, com.example.petcare.domain.model.PetLocation(0.0,0.0), 5000),
            isLoading = false
        )
    }
}

// You'll need a default pet avatar drawable, e.g., app/src/main/res/drawable/ic_default_pet_avatar.xml
// For example, a simple vector drawable:
/*
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path
      android:fillColor="@android:color/white"
      android:pathData="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"/>
</vector>
*/