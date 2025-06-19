package com.example.petcare.presentation.ui.navigation

   import androidx.compose.foundation.layout.Box
   import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
   import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.petcare.presentation.ui.screens.ranking.RankingScreen // Import Ranking Screen
import com.example.petcare.presentation.ui.screens.aichatbot.AiChatbotScreen // Import AI Chatbot Screen
import com.example.petcare.presentation.ui.screens.walking.WalkingScreen // Import Walking Screen
import com.example.petcare.presentation.ui.screens.home.HomeScreen // Import Home Screen
import com.example.petcare.presentation.ui.screens.settings.SettingsScreen
import com.example.petcare.presentation.viewmodel.RankingViewModel // Import Ranking ViewModel
import com.example.petcare.presentation.viewmodel.AiChatbotViewModel // Import AI Chatbot ViewModel
import com.example.petcare.presentation.viewmodel.WalkingViewModel // Import Walking ViewModel
import com.example.petcare.presentation.viewmodel.SettingsViewModel
import com.example.petcare.presentation.viewmodel.HomeViewModel // Import Home ViewModel
import com.example.petcare.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel() // For checking initial profile status
) {
    val initialRouteDecision by mainViewModel.initialRoute.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route, // Always start with Splash
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Splash.route) {
            LaunchedEffect(initialRouteDecision) {
                if (initialRouteDecision.isNotEmpty() && initialRouteDecision != Screen.Splash.route) {
                    navController.navigate(initialRouteDecision) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            // You can show a loading indicator or your app logo here
            Box(modifier = Modifier.fillMaxSize()) { /* Loading or Splash UI */ }
        }
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(navController = navController, viewModel = settingsViewModel)
        }
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(Screen.Walking.route) {
            val walkingViewModel: WalkingViewModel = hiltViewModel()
            WalkingScreen(navController = navController, viewModel = walkingViewModel)
        }
        composable(Screen.AiChatbot.route) {
            val aiChatbotViewModel: AiChatbotViewModel = hiltViewModel()
            AiChatbotScreen(navController = navController, viewModel = aiChatbotViewModel)
        }
        composable(Screen.Ranking.route) {
            val rankingViewModel: RankingViewModel = hiltViewModel()
            RankingScreen(navController = navController, viewModel = rankingViewModel)
        }
    }
}