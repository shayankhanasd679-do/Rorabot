package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RoraBotLogo
import com.example.ui.screens.BlockListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.screens.TrainBrainScreen
import com.example.ui.screens.WhatsAppConnectionScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppSubtext
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppTealGreen

enum class RoraBotNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Home),
    CONNECTION("connection", "Link QR", Icons.Default.QrCodeScanner),
    NEVER_REPLY("never_reply", "VIP List", Icons.Default.Block),
    TRAIN_BRAIN("train_brain", "Train Brain", Icons.Default.Psychology),
    SIMULATOR("simulator", "Simulator", Icons.Default.Chat),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoraBotApp(
    viewModel: RoraBotViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(RoraBotNavDestination.DASHBOARD) }
    val blockedCount by viewModel.blockedCount.collectAsState()
    val isAutoReplyEnabled by viewModel.isAutoReplyEnabled.collectAsState()
    val isConnected by viewModel.isWhatsAppConnected.collectAsState()

    // Handle back button when not on Dashboard
    BackHandler(enabled = currentDestination != RoraBotNavDestination.DASHBOARD) {
        currentDestination = RoraBotNavDestination.DASHBOARD
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = WhatsAppDarkGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoraBotLogo(
                            size = 38.dp,
                            showBackground = true
                        )

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "RoraBot",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                // Active status pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAutoReplyEnabled) WhatsAppLightGreen.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isAutoReplyEnabled) "ONLINE" else "PAUSED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isAutoReplyEnabled) WhatsAppLightGreen else Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Shayan's Pashto Auto Reply • Peshawar",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFD0E8E4),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // QR link quick icon
                    IconButton(
                        onClick = { currentDestination = RoraBotNavDestination.CONNECTION },
                        modifier = Modifier.testTag("top_bar_qr_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Link QR",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = WhatsAppSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                RoraBotNavDestination.values().forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            if (dest == RoraBotNavDestination.NEVER_REPLY && blockedCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = AlertRed) {
                                            Text(
                                                text = "$blockedCount",
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WhatsAppDarkGreen,
                            selectedTextColor = WhatsAppDarkGreen,
                            unselectedIconColor = WhatsAppSubtext,
                            unselectedTextColor = WhatsAppSubtext,
                            indicatorColor = WhatsAppLightGreen.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentDestination,
                label = "navigationTransition"
            ) { destination ->
                when (destination) {
                    RoraBotNavDestination.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToConnection = { currentDestination = RoraBotNavDestination.CONNECTION },
                        onNavigateToBlockList = { currentDestination = RoraBotNavDestination.NEVER_REPLY },
                        onNavigateToTrainBrain = { currentDestination = RoraBotNavDestination.TRAIN_BRAIN },
                        onNavigateToSimulator = { currentDestination = RoraBotNavDestination.SIMULATOR }
                    )
                    RoraBotNavDestination.CONNECTION -> WhatsAppConnectionScreen(
                        viewModel = viewModel
                    )
                    RoraBotNavDestination.NEVER_REPLY -> BlockListScreen(
                        viewModel = viewModel
                    )
                    RoraBotNavDestination.TRAIN_BRAIN -> TrainBrainScreen(
                        viewModel = viewModel
                    )
                    RoraBotNavDestination.SIMULATOR -> SimulatorScreen(
                        viewModel = viewModel
                    )
                    RoraBotNavDestination.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
