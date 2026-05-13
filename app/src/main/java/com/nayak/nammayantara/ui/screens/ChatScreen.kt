package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.ChatMessage
import com.nayak.nammayantara.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val viewModel: ChatViewModel = viewModel()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(viewModel.messages.size - 1)
            }
        }
    }

    fun sendMessage() {
        if (inputText.isNotBlank() && !viewModel.isLoading) {
            viewModel.sendMessage(inputText)
            inputText = ""
        }
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // AI avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(YantraTeal, YantraAmber)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) { Text("🤖", fontSize = 18.sp) }

                        Column {
                            Text(
                                "AI Farming Assistant",
                                style      = MaterialTheme.typography.titleMedium,
                                color      = YantraWhite,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Powered by Gemini",
                                style = MaterialTheme.typography.labelSmall,
                                color = YantraTeal,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        },
        bottomBar = {
            Surface(
                color         = YantraSurface,
                shadowElevation = 12.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value         = inputText,
                        onValueChange = { inputText = it },
                        placeholder   = { Text("Ask about tractors, prices…", color = YantraGrey30, style = MaterialTheme.typography.bodyMedium) },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(24.dp),
                        maxLines      = 3,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = YantraAmber,
                            unfocusedBorderColor    = YantraGrey30,
                            focusedTextColor        = YantraWhite,
                            unfocusedTextColor      = YantraWhite,
                            cursorColor             = YantraAmber,
                            focusedContainerColor   = YantraSurfaceHigh,
                            unfocusedContainerColor = YantraSurfaceHigh,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    )
                    // Send FAB
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) YantraAmber else YantraGrey30
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = { sendMessage() }) {
                            Icon(
                                Icons.Rounded.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) YantraAsphalt else YantraGrey60,
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(YantraAsphalt)
                .padding(padding),
        ) {
            // Quick suggestion chips — shown only before first user message
            if (viewModel.messages.size <= 1) {
                val suggestions = listOf(
                    "🚜 Best tractor for 3 acres of paddy?",
                    "💰 Daily rate for a harvester?",
                    "📅 How do I book a machine?",
                    "🌾 Machine for sugarcane harvesting?",
                    "🔧 What fuel do tractors use?",
                )
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        "Quick questions",
                        style = MaterialTheme.typography.labelSmall,
                        color = YantraGrey60,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick   = { viewModel.sendMessage(suggestion) },
                            label     = { Text(suggestion, style = MaterialTheme.typography.bodySmall, color = YantraWhite) },
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape     = RoundedCornerShape(10.dp),
                            colors    = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = YantraSurface,
                            ),
                            border    = SuggestionChipDefaults.suggestionChipBorder(
                                enabled      = true,
                                borderColor  = YantraGrey30,
                                borderWidth  = 0.5.dp,
                            ),
                        )
                    }
                }
            }

            // Messages
            LazyColumn(
                state           = listState,
                modifier        = Modifier.weight(1f).fillMaxWidth(),
                contentPadding  = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message = message)
                }

                // Thinking indicator
                if (viewModel.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(YantraTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) { Text("🤖", fontSize = 16.sp) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                    .background(YantraSurface)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(14.dp),
                                        color       = YantraTeal,
                                        strokeWidth = 2.dp,
                                    )
                                    Text(
                                        "Thinking…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = YantraGrey60,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom,
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(YantraTeal.copy(alpha = 0.15f))
                    .border(1.dp, YantraTeal.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("🤖", fontSize = 15.sp) }
            Spacer(Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                    else        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(YantraAmber, YantraAmberDim))
                    else
                        Brush.linearGradient(listOf(YantraSurface, YantraSurface))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text      = message.text,
                style     = MaterialTheme.typography.bodyMedium,
                color     = if (isUser) YantraAsphalt else YantraWhite,
                lineHeight = 22.sp,
            )
        }

        if (isUser) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(YantraAmber.copy(alpha = 0.15f))
                    .border(1.dp, YantraAmber.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("👤", fontSize = 15.sp) }
        }
    }
}