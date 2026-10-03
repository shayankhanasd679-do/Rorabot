package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.BrainRule
import com.example.ui.RoraBotViewModel
import com.example.ui.theme.PashtoGold
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppBorder
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppSubtext
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppTealGreen
import com.example.util.ChatExportParser

@Composable
fun TrainBrainScreen(
    viewModel: RoraBotViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Option A: Chat Export", "Option B: Manual Form", "Option C: Corrections")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppBackground)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WhatsAppSurface,
            contentColor = WhatsAppDarkGreen,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = WhatsAppDarkGreen,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ChatExportTab(viewModel)
            1 -> ManualTrainingTab(viewModel)
            2 -> CorrectionsTab(viewModel)
        }
    }
}

@Composable
private fun ChatExportTab(viewModel: RoraBotViewModel) {
    var rawText by remember { mutableStateOf("") }
    val lastResult by viewModel.lastExportResult.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    rawText = content
                    viewModel.parseAndLearnChatExport(content)
                }
            } catch (e: Exception) {
                // handle gracefully
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = WhatsAppDarkGreen
                        )
                        Text(
                            text = "Option A: Upload Chat Export (.txt)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDark
                            )
                        )
                    }
                    Text(
                        text = "Export any WhatsApp chat without media. RoraBot will parse Shayan's replies and extract vocabulary like 'rora', 'busy yam', 'kha', and 'tori der baad'.",
                        style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch("text/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppTealGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_chat_txt_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select .txt File", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                rawText = ChatExportParser.SAMPLE_CHAT_EXPORT.trim()
                                viewModel.parseAndLearnChatExport(rawText)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("load_sample_chat_button")
                        ) {
                            Text("Load Sample", color = WhatsAppDarkGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                label = { Text("Or Paste Chat Export Text Here") },
                placeholder = { Text("[02/10/2026, 11:15 AM] Bilal: Salam!\n[02/10/2026, 11:15 AM] Shayan: Walaikum Salam rora...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("chat_export_textarea"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        viewModel.parseAndLearnChatExport(rawText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parse_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parse & Train Brain", fontWeight = FontWeight.Bold)
            }
        }

        if (lastResult != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F9EE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WhatsAppLightGreen, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WhatsAppDarkGreen
                            )
                            Text(
                                text = "Training Complete!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppDarkGreen
                                )
                            )
                        }

                        Text(
                            text = lastResult!!.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(color = WhatsAppDark)
                        )

                        Text(
                            text = "Extracted Keywords:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDarkGreen
                            )
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(lastResult!!.detectedKeywords) { kw ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WhatsAppSurface)
                                        .border(1.dp, WhatsAppBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = kw,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = WhatsAppDarkGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ManualTrainingTab(viewModel: RoraBotViewModel) {
    var whenFriendSays by remember { mutableStateOf("") }
    var youReply by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("GENERAL") }

    val categories = listOf("GREETING", "ASKING_LOCATION", "ASKING_WELLBEING", "GENERAL", "CUSTOM")
    val allRules by viewModel.brainRules.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Option B: Manual Training Form",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppDark
                        )
                    )
                    Text(
                        text = "Directly teach the AI Brain how Shayan responds to specific phrases.",
                        style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                    )

                    OutlinedTextField(
                        value = whenFriendSays,
                        onValueChange = { whenFriendSays = it },
                        label = { Text("When friend says...") },
                        placeholder = { Text("e.g. chai piyo ge? / kahan milo ge?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("when_friend_says_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = youReply,
                        onValueChange = { youReply = it },
                        label = { Text("You reply (Roman Pashto)...") },
                        placeholder = { Text("e.g. Namak Mandi ke chai kho pakka peewu rora!") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("you_reply_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Intent Category:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WhatsAppDark
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WhatsAppDarkGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (whenFriendSays.isNotBlank() && youReply.isNotBlank()) {
                                viewModel.addBrainRule(whenFriendSays, youReply, selectedCategory)
                                whenFriendSays = ""
                                youReply = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_rule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Brain Knowledge Base", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Trained Knowledge Base (${allRules.size} rules)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppDark
                )
            )
        }

        items(allRules, key = { it.id }) { rule ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhatsAppBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WhatsAppDarkGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rule.intentCategory,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppDarkGreen
                                    )
                                )
                            }
                            Text(
                                text = "Source: ${rule.source}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WhatsAppSubtext,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Text(
                            text = "Friend: \"${rule.triggerPattern}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDark
                            )
                        )

                        Text(
                            text = "Reply: \"${rule.replyText}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WhatsAppDarkGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    if (rule.source != "SYSTEM") {
                        IconButton(onClick = { viewModel.deleteBrainRule(rule.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = WhatsAppSubtext
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CorrectionsTab(viewModel: RoraBotViewModel) {
    val recentLogs by viewModel.recentLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Option C: Correction & Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppDark
                        )
                    )
                    Text(
                        text = "Review how the bot replied. Tap 'Correct' on any response to override the AI and train it for future conversations.",
                        style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                    )
                }
            }
        }

        items(recentLogs) { log ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhatsAppBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${log.senderName} (${log.detectedIntent})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDark
                            )
                        )

                        Text(
                            text = log.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (log.wasBlocked) Color.Red else WhatsAppDarkGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "Incoming: \"${log.incomingText}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(color = WhatsAppDark)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE7FFDB))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Replied: \"${log.replyText}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WhatsAppDarkGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    if (!log.wasBlocked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.startCorrectingLog(log) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("correct_tab_button_${log.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Correct This Reply", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
