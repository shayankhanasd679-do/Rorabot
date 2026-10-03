package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RoraBotViewModel
import com.example.ui.SimulatorMessage
import com.example.ui.theme.AlertRed
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppBorder
import com.example.ui.theme.WhatsAppChatBg
import com.example.ui.theme.WhatsAppChatBubbleReceiver
import com.example.ui.theme.WhatsAppChatBubbleSender
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppGrayTick
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppSubtext
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppTealGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: RoraBotViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.simulatorMessages.collectAsState()
    val isBotTyping by viewModel.isBotTyping.collectAsState()
    val selectedContact by viewModel.selectedSimulatorContact.collectAsState()
    val isAutoReplyEnabled by viewModel.isAutoReplyEnabled.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isBotTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val contactsList = listOf(
        "Bilal Hayatabad (+923339123456)",
        "VIP Number 1 (+923005747688)",
        "VIP Number 2 (+923458009193)"
    )

    val quickTestPrompts = listOf(
        "Salam rora!" to false,
        "Kaha ho abhi?" to false,
        "Singa ye rora?" to false,
        "Chai khwro saba?" to false,
        "🎙️ Audio Note: Kahan ho?" to true
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppChatBg)
    ) {
        // WhatsApp Chat Header
        Surface(
            color = WhatsAppDarkGreen,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedContact.contains("VIP")) AlertRed.copy(alpha = 0.8f)
                                else WhatsAppLightGreen
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedContact.contains("VIP")) "VIP" else "BH",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Column {
                        Text(
                            text = selectedContact.substringBefore(" ("),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isBotTyping) "Shayan typing..."
                            else if (selectedContact.contains("VIP")) "Never Reply VIP List"
                            else if (isAutoReplyEnabled) "RoraBot Active"
                            else "RoraBot Paused",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isBotTyping) WhatsAppLightGreen else Color(0xFFE0E0E0),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.clearSimulator() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Contact Switcher Bar
        Surface(
            color = WhatsAppSurface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "Select Friend / Sender:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WhatsAppSubtext,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contactsList) { contact ->
                        val isSelected = selectedContact == contact
                        val isVip = contact.contains("VIP")
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectSimulatorContact(contact) },
                            label = {
                                Text(
                                    text = if (isVip) "🛡️ ${contact.substringBefore(" (")}" else "👤 ${contact.substringBefore(" (")}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isVip) AlertRed else WhatsAppDarkGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // VIP Protection Alert Notice if VIP Contact is selected
        if (selectedContact.contains("VIP")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "VIP Protected: Messages from this number will stay UNREAD. Zero bot reply.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Quick Test Prompt Chips
        Surface(
            color = Color(0xFFECE5DD),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickTestPrompts) { (prompt, isVoice) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(WhatsAppSurface)
                            .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.sendSimulatorMessage(prompt, isVoice) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WhatsAppDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg = msg)
            }

            if (isBotTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(WhatsAppChatBubbleSender)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Shayan is typing in Roman Pashto...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WhatsAppDarkGreen,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bottom Input Row
        Surface(
            color = Color(0xFF1F2C34),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Type message as friend (e.g. Salam)...",
                            fontSize = 13.sp,
                            color = Color(0xFF8696A0)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1F2C34),
                        unfocusedContainerColor = Color(0xFF1F2C34),
                        disabledContainerColor = Color(0xFF1F2C34),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color(0xFF8696A0),
                        unfocusedPlaceholderColor = Color(0xFF8696A0),
                        cursorColor = Color(0xFF25D366),
                        focusedBorderColor = Color(0xFF25D366),
                        unfocusedBorderColor = Color(0xFF2A3942)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulator_chat_input"),
                    maxLines = 3
                )

                // Voice Note button
                IconButton(
                    onClick = {
                        viewModel.sendSimulatorMessage("🎙️ [Voice Note: 0:08]", isVoice = true)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsAppTealGreen)
                        .testTag("simulator_voice_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Send Voice Note",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Send button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendSimulatorMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsAppDarkGreen)
                        .testTag("simulator_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: SimulatorMessage) {
    val isSender = msg.isFromShayanBot
    val timeFormatted = remember(msg.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(msg.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isSender) 14.dp else 2.dp,
                bottomEnd = if (isSender) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSender) WhatsAppChatBubbleSender else WhatsAppChatBubbleReceiver
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header if intent tag exists
                if (msg.intentTag != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WhatsAppDarkGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Intent: ${msg.intentTag}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppDarkGreen
                                )
                            )
                        }
                    }
                }

                // Voice note UI
                if (msg.isVoiceNote) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WhatsAppLightGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Voice Message",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "0:08 • Transcribed by Brain",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WhatsAppSubtext,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WhatsAppDark,
                        fontWeight = if (isSender) FontWeight.Medium else FontWeight.Normal
                    )
                )

                // Timestamp & Double tick
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = WhatsAppSubtext
                        )
                    )

                    if (isSender) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = if (msg.isRead) "Read (Blue Ticks)" else "Delivered (Gray Ticks)",
                            tint = if (msg.isRead) WhatsAppBlueTick else WhatsAppGrayTick,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
