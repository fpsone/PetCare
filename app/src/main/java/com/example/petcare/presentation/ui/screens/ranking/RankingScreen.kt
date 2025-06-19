package com.example.petcare.presentation.ui.screens.ranking

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.petcare.R
import com.example.petcare.domain.model.RankedPet
import com.example.petcare.presentation.mvi.RankingContract
import com.example.petcare.presentation.ui.components.ShimmerPlaceholder
import com.example.petcare.presentation.ui.theme.Bronze
import com.example.petcare.presentation.ui.theme.Gold
import com.example.petcare.presentation.ui.theme.PetCareTheme
import com.example.petcare.presentation.ui.theme.Silver
import com.example.petcare.presentation.viewmodel.RankingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RankingScreen(
    navController: NavController,
    viewModel: RankingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RankingContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pet Leaderboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendEvent(RankingContract.Event.RefreshRankings) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Rankings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.rankings.isEmpty()) {
            RankingLoadingShimmer(paddingValues)
        } else if (uiState.error != null) {
            ErrorStateView(
                message = uiState.error!!,
                onRetry = { viewModel.sendEvent(RankingContract.Event.RefreshRankings) },
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            )
        } else if (uiState.rankings.isEmpty()) {
            EmptyStateView(
                message = "No rankings available yet. Check back soon!",
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            )
        } else {
            RankingList(
                rankings = uiState.rankings,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun RankingList(rankings: List<RankedPet>, modifier: Modifier = Modifier) {
    val top3 = rankings.take(3)
    val rest = rankings.drop(3)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top 3 Podium
        if (top3.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Rank 2
                    if (top3.size >= 2) {
                        PodiumItem(pet = top3[1], medalColor = Silver, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                    } else { Spacer(modifier = Modifier.weight(1f)) }
                    // Rank 1
                    PodiumItem(pet = top3[0], medalColor = Gold, isCenter = true, modifier = Modifier.weight(1.2f).padding(horizontal = 4.dp))
                    // Rank 3
                    if (top3.size >= 3) {
                        PodiumItem(pet = top3[2], medalColor = Bronze, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                    } else { Spacer(modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Ranks 4-10
        itemsIndexed(rest, key = { _, item -> item.id }) { index, pet ->
            val animationState = remember { MutableTransitionState(false) }
            LaunchedEffect(key1 = pet.id) {
                delay(index * 100L) // Stagger animation
                animationState.targetState = true
            }
            AnimatedVisibility(
                visibleState = animationState,
                enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = tween(300))
            ) {
                RankedPetListItem(pet = pet, modifier = Modifier) // animateItemPlacement can be added if desired, ensure @OptIn(ExperimentalFoundationApi::class) is at function or call site
            }
        }
    }
}

@Composable
fun PodiumItem(pet: RankedPet, medalColor: Color, modifier: Modifier = Modifier, isCenter: Boolean = false) {
    val elevation = if (isCenter) 12.dp else 8.dp
    val scale = if (isCenter) 1.0f else 0.9f
    val cardHeight = if (isCenter) 180.dp else 160.dp

    Card(
        modifier = modifier.height(cardHeight * scale).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = "Medal", tint = medalColor, modifier = Modifier.size(36.dp * scale))
            Spacer(modifier = Modifier.height(8.dp * scale))
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pet.avatarUrl ?: R.drawable.ic_default_pet_avatar)
                        .crossfade(true).build(),
                    error = painterResource(id = R.drawable.ic_default_pet_avatar)
                ),
                contentDescription = pet.name,
                modifier = Modifier.size(60.dp * scale).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp * scale))
            Text(pet.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp * scale), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("${pet.score} pts", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp * scale), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RankedPetListItem(pet: RankedPet, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        ListItem(
            headlineContent = { Text(pet.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("${pet.score} activity points") },
            leadingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("#${pet.rank}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(30.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pet.avatarUrl ?: R.drawable.ic_default_pet_avatar)
                                .crossfade(true).build(),
                            error = painterResource(id = R.drawable.ic_default_pet_avatar)
                        ),
                        contentDescription = pet.name,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        )
    }
}

@Composable
fun RankingLoadingShimmer(paddingValues: PaddingValues) {
    Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ShimmerPlaceholder(height = 140.dp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp), shapeRadius = 12.dp)
            ShimmerPlaceholder(height = 180.dp, modifier = Modifier.weight(1.2f).padding(horizontal = 4.dp), shapeRadius = 12.dp)
            ShimmerPlaceholder(height = 140.dp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp), shapeRadius = 12.dp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        repeat(4) {
            ShimmerPlaceholder(height = 70.dp, shapeRadius = 12.dp)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun EmptyStateView(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.Pets, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankingScreenPreview() {
    PetCareTheme {
        // RankingScreen(navController = rememberNavController()) // Needs Hilt
        RankingLoadingShimmer(paddingValues = PaddingValues(0.dp))
    }
}