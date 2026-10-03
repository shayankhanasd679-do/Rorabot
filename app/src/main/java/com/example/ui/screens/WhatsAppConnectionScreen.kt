package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RoraBotViewModel
import com.example.ui.components.RoraBotLogo
import com.example.ui.theme.AlertRed
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppBorder
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppSubtext
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppTealGreen

@Composable
fun WhatsAppConnectionScreen(
    viewModel: RoraBotViewModel,
    modifier: Modifier = Modifier
) {
    val isConnected by viewModel.isWhatsAppConnected.collectAsState()
    var qrSeed by remember { mutableIntStateOf(1) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Badge Header
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(if (isConnected) Color(0xFFE7F9EE) else Color(0xFFFFF0F0))
                .border(
                    width = 1.dp,
                    color = if (isConnected) WhatsAppLightGreen else AlertRed,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                text = if (isConnected) "WhatsApp Status: Connected" else "WhatsApp Status: Disconnected",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) WhatsAppDarkGreen else AlertRed
                )
            )
        }

        // Instructions banner specified in prompt:
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Scan with your WhatsApp:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppDark
                    )
                )
                Text(
                    text = "WhatsApp > Settings > Linked Devices > Link a Device",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WhatsAppDarkGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Point your camera to pair Shayan's Baileys multi-device session.",
                    style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
                )
            }
        }

        // Big QR Code Display Area
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhatsAppBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // QR Canvas
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, WhatsAppBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeMatrixCanvas(
                        seed = qrSeed,
                        isScanning = !isConnected,
                        modifier = Modifier.size(216.dp)
                    )

                    // Logo badge in center of QR
                    RoraBotLogo(
                        size = 46.dp,
                        showBackground = true
                    )
                }

                if (isConnected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Linked with Shayan's WhatsApp (+92-333-Peshawar)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppDark
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Awaiting scan from your mobile phone...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WhatsAppSubtext,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { qrSeed++ },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("regenerate_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = WhatsAppDarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh QR",
                            color = WhatsAppDarkGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleWhatsAppConnection(!isConnected) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) AlertRed else WhatsAppDarkGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_connection_button")
                    ) {
                        Text(
                            text = if (isConnected) "Disconnect" else "Simulate Link",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // WhatsApp Baileys Library Logic Information Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = WhatsAppDarkGreen
                    )
                    Text(
                        text = "Baileys Multi-Device Core",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppDark
                        )
                    )
                }

                BaileysInfoRow(label = "Library Engine", value = "@whiskeysockets/baileys v6.7.12")
                BaileysInfoRow(label = "WebSocket Handshake", value = "Noise_XX_25519_AESGCM_SHA256")
                BaileysInfoRow(label = "Session Store", value = "Encrypted Local Room DB")
                BaileysInfoRow(label = "Intent Routing", value = "Single Unified Brain Engine")
                BaileysInfoRow(label = "Push Keep-Alive", value = "Active (30s interval)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BaileysInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = WhatsAppSubtext)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = WhatsAppDark,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun QrCodeMatrixCanvas(
    seed: Int,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanY"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gridSize = 21
        val cellSize = w / gridSize

        // Draw standard QR Finder Patterns at top-left, top-right, bottom-left
        drawFinderPattern(0f, 0f, cellSize)
        drawFinderPattern(cellSize * (gridSize - 7), 0f, cellSize)
        drawFinderPattern(0f, cellSize * (gridSize - 7), cellSize)

        // Draw simulated modules based on deterministic pseudo-random seed
        val rand = java.util.Random((seed * 7919).toLong())
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                // Skip finder areas
                val inTopLeft = r < 7 && c < 7
                val inTopRight = r < 7 && c >= gridSize - 7
                val inBottomLeft = r >= gridSize - 7 && c < 7
                val inCenter = r in 7..13 && c in 7..13

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
                    if (rand.nextBoolean()) {
                        drawRoundRect(
                            color = WhatsAppDark,
                            topLeft = Offset(c * cellSize + cellSize * 0.1f, r * cellSize + cellSize * 0.1f),
                            size = Size(cellSize * 0.8f, cellSize * 0.8f),
                            cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                        )
                    }
                }
            }
        }

        // Draw animated laser scanning line if not connected
        if (isScanning) {
            val laserY = h * scanLineProgress
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WhatsAppLightGreen,
                        WhatsAppDarkGreen,
                        WhatsAppLightGreen,
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(w, laserY),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    cellSize: Float
) {
    val outerSize = cellSize * 7
    // Outer black square
    drawRoundRect(
        color = WhatsAppDark,
        topLeft = Offset(x, y),
        size = Size(outerSize, outerSize),
        cornerRadius = CornerRadius(cellSize * 0.6f, cellSize * 0.6f)
    )
    // White border
    val innerWhiteOffset = cellSize * 1f
    val innerWhiteSize = cellSize * 5f
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x + innerWhiteOffset, y + innerWhiteOffset),
        size = Size(innerWhiteSize, innerWhiteSize),
        cornerRadius = CornerRadius(cellSize * 0.4f, cellSize * 0.4f)
    )
    // Inner solid square
    val innerBlackOffset = cellSize * 2f
    val innerBlackSize = cellSize * 3f
    drawRoundRect(
        color = WhatsAppDarkGreen,
        topLeft = Offset(x + innerBlackOffset, y + innerBlackOffset),
        size = Size(innerBlackSize, innerBlackSize),
        cornerRadius = CornerRadius(cellSize * 0.4f, cellSize * 0.4f)
    )
}
