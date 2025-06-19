package com.example.petcare.presentation.ui.screens.aichatbot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.petcare.domain.model.ChatMessage
import com.example.petcare.presentation.mvi.AiChatbotContract
import com.example.petcare.presentation.ui.theme.PetCareTheme
import com.example.petcare.presentation.viewmodel.AiChatbotViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

   @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class) // Apply OptIn directly to the function
@Composable
fun AiChatbotScreen(
    navController: NavController,
    viewModel: AiChatbotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AiChatbotContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PetPal AI Assistant") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Column {
                SuggestionChipsRow(
                    suggestions = uiState.recommendedQuestions,
                    onSuggestionClick = { viewModel.sendEvent(AiChatbotContract.Event.OnRecommendedQuestionClick(it)) },
                    enabled = uiState.isPetProfileLoaded && !uiState.isLoadingResponse
                )
                MessageInputBar(
                    value = uiState.currentMessageInput,
                    onValueChange = { viewModel.sendEvent(AiChatbotContract.Event.OnMessageInputChange(it)) },
                    onSendClick = {
                        viewModel.sendEvent(AiChatbotContract.Event.OnSendMessageClick)
                        focusManager.clearFocus()
                    },
                    enabled = uiState.isPetProfileLoaded && !uiState.isLoadingResponse
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (!uiState.isPetProfileLoaded && uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(uiState.messages, key = { _: Int, item: ChatMessage -> item.id }) { index: Int, message: ChatMessage ->
                        ChatMessageBubble(message = message, modifier = Modifier.animateItemPlacement())
                    }
                    if (uiState.isLoadingResponse) {
                        item { TypingIndicator(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isFromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp), contentAlignment = alignment) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.animateContentSize(animationSpec = tween(300))
                           // slideInVertically and fadeIn as used here are EnterTransitions and cannot be directly applied to Modifier.
                           // animateItemPlacement (on the parent modifier) and animateContentSize handle related animations.
                           // If specific enter/exit for the Card is needed, wrap it with AnimatedVisibility.
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun MessageInputBar(value: String, onValueChange: (String) -> Unit, onSendClick: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask PetPal...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendClick() }),
            shape = RoundedCornerShape(24.dp),
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendClick, enabled = enabled && value.isNotBlank()) {
            Icon(Icons.Filled.Send, contentDescription = "Send Message", tint = if (enabled && value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
        }
    }
}

@Composable
fun SuggestionChipsRow(suggestions: List<String>, onSuggestionClick: (String) -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { if(enabled) onSuggestionClick(suggestion) },
                label = { Text(suggestion) },
                enabled = enabled,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        (1..3).forEach { index ->
            val alpha = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(index * 200L) // Stagger animation
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(8.dp)
                    .alpha(alpha.value)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("PetPal is typing...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun AiChatbotScreenPreview() {
    PetCareTheme {
        // AiChatbotScreen(navController = rememberNavController()) // Needs Hilt
        Column {
            ChatMessageBubble(ChatMessage("1", "Hello User!", 1L, false))
            ChatMessageBubble(ChatMessage("2", "Hello PetPal!", 2L, true))
            TypingIndicator()
            MessageInputBar(value = "Test", onValueChange = {}, onSendClick = {}, enabled = true)
            SuggestionChipsRow(suggestions = listOf("Suggestion 1", "Suggestion 2"), onSuggestionClick = {}, enabled = true)
        }
    }
}