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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AutoReplyLog
import com.example.ui.RoraBotViewModel
import com.example.ui.components.RoraBotLogo
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PashtoGold
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppBorder
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGrayTick
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppSubtext
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppTealGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: RoraBotViewModel,
    onNavigateToConnection: () -> Unit,
    onNavigateToBlockList: () -> Unit,
    onNavigateToTrainBrain: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAutoReplyEnabled by viewModel.isAutoReplyEnabled.collectAsState()
    val isWhatsAppConnected by viewModel.isWhatsAppConnected.collectAsState()
    val totalReplies by viewModel.totalRepliesCount.collectAsState()
    val activeChats by viewModel.activeChatsCount.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val vipBlockedEvents by viewModel.vipBlockedEventsCount.collectAsState()
    val brainRulesCount by viewModel.brainRulesCount.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val correctingLog by viewModel.correctingLog.collectAsState()

    var correctionText by remember(correctingLog) {
        mutableStateOf(correctingLog?.replyText ?: "")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Status & Master Switch Card
            MasterSwitchCard(
                isEnabled = isAutoReplyEnabled,
                isConnected = isWhatsAppConnected,
                onToggle = { viewModel.toggleAutoReply(it) },
                onConnectClick = onNavigateToConnection
            )
        }

        item {
            // Metrics Overview Grid
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Replies Sent",
                    value = "$totalReplies",
                    subtext = "Today",
                    icon = Icons.Default.Chat,
                    accentColor = WhatsAppLightGreen
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Chats",
                    value = "$activeChats",
                    subtext = "Conversations",
                    icon = Icons.Default.FlashOn,
                    accentColor = WhatsAppTealGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Never Reply VIPs",
                    value = "$blockedCount",
                    subtext = "$vipBlockedEvents Blocked",
                    icon = Icons.Default.Block,
                    accentColor = AlertRed,
                    onClick = onNavigateToBlockList
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Brain Rules",
                    value = "$brainRulesCount",
                    subtext = "Peshawari Roman",
                    icon = Icons.Default.Psychology,
                    accentColor = PashtoGold,
                    onClick = onNavigateToTrainBrain
                )
            }
        }

        item {
            // Quick Navigation Action Pills
            Text(
                text = "Quick Controls",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppDark
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Test Simulator",
                    subtitle = "Interactive Chat",
                    icon = Icons.Default.Tune,
                    color = WhatsAppDarkGreen,
                    onClick = onNavigateToSimulator
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Train Brain",
                    subtitle = "Chat & Rules",
                    icon = Icons.Default.Psychology,
                    color = WhatsAppTealGreen,
                    onClick = onNavigateToTrainBrain
                )
            }
        }

        item {
            // Live Activity Feed Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Activity & Logs",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppDark
                    )
                )
                Text(
                    text = "${recentLogs.size} events",
                    style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                )
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = WhatsAppGrayTick,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No auto-reply events yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Test an incoming message in the Test Simulator to see live processing.",
                            style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentLogs) { log ->
                LogItemCard(
                    log = log,
                    onCorrectClick = { viewModel.startCorrectingLog(log) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Correction Dialog for Option C
    if (correctingLog != null) {
        Dialog(onDismissRequest = { viewModel.dismissCorrection() }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = WhatsAppSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = WhatsAppDarkGreen
                        )
                        Text(
                            text = "Correct AI Auto-Reply",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "When friend (${correctingLog?.senderName}) said:",
                        style = MaterialTheme.typography.labelMedium.copy(color = WhatsAppSubtext)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WhatsAppBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"${correctingLog?.incomingText}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Text(
                        text = "Your corrected reply (Saved to Brain):",
                        style = MaterialTheme.typography.labelMedium.copy(color = WhatsAppSubtext)
                    )
                    OutlinedTextField(
                        value = correctionText,
                        onValueChange = { correctionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("correction_reply_input"),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Write how Shayan should reply...") },
                        maxLines = 4
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.dismissCorrection() }) {
                            Text("Cancel", color = WhatsAppSubtext)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (correctionText.isNotBlank()) {
                                    viewModel.saveLogCorrection(correctionText)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_correction_button")
                        ) {
                            Text("Save to Brain")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterSwitchCard(
    isEnabled: Boolean,
    isConnected: Boolean,
    onToggle: (Boolean) -> Unit,
    onConnectClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isEnabled) WhatsAppLightGreen.copy(alpha = 0.5f) else WhatsAppBorder,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RoraBotLogo(size = 52.dp)
                    Column {
                        Text(
                            text = "RoraBot Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDark
                            )
                        )
                        Text(
                            text = if (isEnabled) "Active • Auto-Replying in Pashto" else "Paused • Double tick only",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) WhatsAppDarkGreen else WhatsAppSubtext,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WhatsAppLightGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = WhatsAppBorder
                    ),
                    modifier = Modifier.testTag("auto_reply_master_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WhatsApp Connection Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isConnected) Color(0xFFE7F9EE) else Color(0xFFFFF0F0))
                    .clickable { onConnectClick() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) WhatsAppLightGreen else AlertRed)
                    )
                    Text(
                        text = if (isConnected) "WhatsApp Linked: Baileys v6.7.12" else "WhatsApp Disconnected",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConnected) WhatsAppDarkGreen else AlertRed
                        )
                    )
                }

                Text(
                    text = if (isConnected) "Manage >" else "Link QR >",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WhatsAppDarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(color = WhatsAppSubtext, fontSize = 10.sp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = WhatsAppDark
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WhatsAppSubtext,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppDark
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WhatsAppSubtext,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun LogItemCard(
    log: AutoReplyLog,
    onCorrectClick: () -> Unit
) {
    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = log.senderName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppDark
                        )
                    )
                    Text(
                        text = "(${log.senderNumber})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WhatsAppSubtext,
                            fontSize = 11.sp
                        )
                    )
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WhatsAppSubtext,
                        fontSize = 11.sp
                    )
                )
            }

            // Incoming message bubble
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (log.isVoiceNote) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Note",
                        tint = WhatsAppBlueTick,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Friend: \"${log.incomingText}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WhatsAppDark,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Outgoing Auto-reply bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (log.wasBlocked) Color(0xFFFFF4F4) else Color(0xFFE7FFDB))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (log.wasBlocked) "🛡️ VIP Blocked (Unread)" else "Shayan (Auto-Reply):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (log.wasBlocked) AlertRed else WhatsAppDarkGreen
                            )
                        )

                        // Intent badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WhatsAppSurface.copy(alpha = 0.8f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = log.detectedIntent,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = WhatsAppDarkGreen
                                )
                            )
                        }
                    }

                    Text(
                        text = log.replyText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WhatsAppDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Bottom action row: correction button
            if (!log.wasBlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCorrectClick,
                        modifier = Modifier.testTag("correct_reply_button_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = WhatsAppDarkGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Correct AI Reply",
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
