package com.example.petcare.presentation.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.rounded.Pets // Example icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.petcare.domain.model.PetGender
import com.example.petcare.presentation.mvi.SettingsContract
import com.example.petcare.presentation.ui.navigation.Screen
import com.example.petcare.presentation.ui.theme.PetCareTheme
import com.example.petcare.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val (showDatePicker, setShowDatePicker) = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is SettingsContract.Effect.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isFirstTimeSetup) "Create Pet Profile" else "Pet Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.sendEvent(SettingsContract.Event.OnSaveClick) },
                enabled = uiState.hasChanges && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Profile")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Pet Profile Section
                SectionHeader("Pet Profile")

                OutlinedTextField(
                    value = uiState.petName,
                    onValueChange = { viewModel.sendEvent(SettingsContract.Event.OnNameChanged(it)) },
                    label = { Text("Pet's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.petBreed,
                    onValueChange = { viewModel.sendEvent(SettingsContract.Event.OnBreedChanged(it)) },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date of Birth
                OutlinedTextField(
                    value = uiState.dateOfBirthTimestamp?.let {
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Select Date of Birth",
                    onValueChange = {},
                    label = { Text("Date of Birth") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setShowDatePicker(true) },
                    trailingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") }
                )
                if (uiState.petAge > 0) {
                    Text("Age: ${uiState.petAge} years old", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Gender
                Text("Gender", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PetGender.entries.filterNot { it == PetGender.UNKNOWN }.forEachIndexed { index, gender ->
                        SegmentedButton(
                            selected = uiState.petGender == gender,
                            onClick = { viewModel.sendEvent(SettingsContract.Event.OnGenderSelected(gender)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = PetGender.entries.size -1)
                        ) {
                            Text(gender.name.lowercase().replaceFirstChar { it.titlecase() })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.petWeight,
                    onValueChange = { viewModel.sendEvent(SettingsContract.Event.OnWeightChanged(it)) },
                    label = { Text("Weight") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(modifier = Modifier.height(16.dp))

                uiState.error?.let { errorText ->
                    if (errorText.isNotBlank()) { // Ensure we only show non-empty errors
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Device Section (Placeholder)
                SectionHeader("Device")
                ListItem(
                    headlineContent = { Text("Wearable Device") },
                    supportingContent = { Text("Status: Connected (Simulated)") }, // Example
                    leadingContent = { Icon(Icons.Rounded.Pets, contentDescription = "Device") }
                )
                Divider()

                Spacer(modifier = Modifier.height(80.dp)) // Space for the bottom button
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        uiState.dateOfBirthTimestamp?.let { calendar.timeInMillis = it }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateOfBirthTimestamp ?: System.currentTimeMillis(),
            yearRange = (Calendar.getInstance().get(Calendar.YEAR) - 30)..(Calendar.getInstance().get(Calendar.YEAR))
        )
        DatePickerDialog(
            onDismissRequest = { setShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // Ensure we use UTC for consistency with DataStore
                        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCalendar.timeInMillis = it
                        // Clear time part to store only date
                        utcCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        utcCalendar.set(Calendar.MINUTE, 0)
                        utcCalendar.set(Calendar.SECOND, 0)
                        utcCalendar.set(Calendar.MILLISECOND, 0)
                        viewModel.sendEvent(SettingsContract.Event.OnDateOfBirthSelected(utcCalendar.timeInMillis))
                    }
                    setShowDatePicker(false)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { setShowDatePicker(false) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PetCareTheme {
        // This preview won't have a real NavController or ViewModel working state
        // It's for basic layout checking.
        val previewUiState = SettingsContract.UiState(
            isLoading = false,
            petName = "Buddy",
            petBreed = "Golden Retriever",
            petAge = 3,
            petGender = PetGender.MALE,
            petWeight = "25.5",
            dateOfBirthTimestamp = Calendar.getInstance().apply { add(Calendar.YEAR, -3) }.timeInMillis,
            hasChanges = true
        )
        // SettingsScreen(navController = rememberNavController(), viewModel = // cannot provide hilt vm here)
        // For a more interactive preview, you'd mock the ViewModel and its state.
    }
}