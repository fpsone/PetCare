package com.example.petcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.domain.usecase.pet.CheckIfProfileExistsUseCase
import com.example.petcare.presentation.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkIfProfileExistsUseCase: CheckIfProfileExistsUseCase
) : ViewModel() {

    private val _initialRoute = MutableStateFlow(Screen.Splash.route) // Start with splash
    val initialRoute = _initialRoute.asStateFlow()

    init {
        viewModelScope.launch {
            // Add a small delay if needed for splash screen visibility, or rely on LaunchedEffect in Splash composable
            // delay(500) // Optional: for splash visibility
            _initialRoute.value = if (checkIfProfileExistsUseCase()) Screen.Home.route else Screen.Settings.route
        }
    }
}