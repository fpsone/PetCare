package com.example.petcare.presentation.ui.screens.walking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import com.example.petcare.domain.model.PetLocation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.petcare.presentation.mvi.WalkingContract
import com.example.petcare.presentation.ui.theme.PetCareTheme
import com.example.petcare.presentation.viewmodel.WalkingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkingScreen(
    navController: NavController,
    viewModel: WalkingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(34.0522, -118.2437), 10f) // Default initial position
    }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is WalkingContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message, duration = SnackbarDuration.Short)
                }
                is WalkingContract.Effect.AnimateMapToLocation -> {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(effect.location, 16f), // Zoom in closer
                        durationMs = 1000
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pet Walking Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoadingMapStyle && uiState.mapStyleJson == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapStyleOptions = uiState.mapStyleJson?.let { MapStyleOptions(it) },
                        isMyLocationEnabled = false, // Set to true if you want user's location
                        latLngBoundsForCameraTarget = null // Can be used to restrict map bounds
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false // Enable if isMyLocationEnabled is true
                    )
                ) {
                    uiState.currentPetLocation?.let { petLoc ->
                        Marker( 
                            state = MarkerState(position = LatLng(petLoc.latitude, petLoc.longitude)),
                            title = "Pet's Location",
                            snippet = "Currently here",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                    if (uiState.locationHistory.size >= 2) {
                        Polyline( 
                            points = uiState.locationHistory.map { loc: PetLocation -> LatLng(loc.latitude, loc.longitude) },
                            color = MaterialTheme.colorScheme.primary,
                            width = 10f
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.sendEvent(WalkingContract.Event.OnSearchQueryChanged(it)) },
                    label = { Text("Search for parks, trails...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.sendEvent(WalkingContract.Event.OnSearchSubmit)
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.weight(1f)) // Pushes stats card to bottom

                AnimatedVisibility(
                    visible = uiState.isPetWalking || uiState.distanceWalkedFormatted != "0.00 km", // Show if walking or has walked
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    LiveStatsOverlay(
                        distance = uiState.distanceWalkedFormatted,
                        duration = uiState.walkDurationFormatted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveStatsOverlay(distance: String, duration: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Distance", style = MaterialTheme.typography.labelMedium)
                Text(distance, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Duration", style = MaterialTheme.typography.labelMedium)
                Text(duration, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalkingScreenPreview() {
    PetCareTheme {
        // WalkingScreen(navController = rememberNavController()) // Needs Hilt and Map context
        LiveStatsOverlay(distance = "1.25 km", duration = "15:30")
    }
}