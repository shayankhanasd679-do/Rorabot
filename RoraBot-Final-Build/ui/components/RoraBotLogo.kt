package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PashtoGold
import com.example.ui.theme.WhatsAppDark
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTealGreen

/**
 * Premium minimal logo - Letter "R" with a Pashto topi (cap) icon, gradient green to dark green.
 */
@Composable
fun RoraBotLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showBackground: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(size * 0.28f))
                        .clip(RoundedCornerShape(size * 0.28f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    WhatsAppDarkGreen,
                                    Color(0xFF0F3E38),
                                    WhatsAppDark
                                )
                            )
                        )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.78f)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Draw Pashto Topi (Pakol Cap) at the top of the "R"
            // Pakol has a flat rolled circular crown and rim
            val capCenterY = h * 0.22f
            val capWidth = w * 0.58f
            val capHeight = h * 0.18f

            // Golden / Warm Earthy Pashto Pakol gradient
            val capBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE2C48D),
                    PashtoGold,
                    Color(0xFF8B6B23)
                )
            )

            // Crown base (oval)
            drawRoundRect(
                brush = capBrush,
                topLeft = Offset((w - capWidth) / 2f, capCenterY - capHeight * 0.45f),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(capHeight * 0.4f, capHeight * 0.4f)
            )

            // Rolled rim of Pakol
            drawRoundRect(
                color = Color(0xFFD6B26A),
                topLeft = Offset((w - capWidth * 1.08f) / 2f, capCenterY + capHeight * 0.15f),
                size = Size(capWidth * 1.08f, capHeight * 0.55f),
                cornerRadius = CornerRadius(capHeight * 0.3f, capHeight * 0.3f)
            )

            // Feather or tassel motif (traditional Peshawari touch)
            val featherPath = Path().apply {
                moveTo(w * 0.65f, capCenterY + capHeight * 0.1f)
                quadraticBezierTo(w * 0.78f, capCenterY - capHeight * 0.6f, w * 0.82f, capCenterY - capHeight * 0.9f)
            }
            drawPath(
                path = featherPath,
                color = WhatsAppLightGreen,
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
            )

            // 2. Draw modern bold Letter "R" with gradient green fill
            val letterBrush = Brush.verticalGradient(
                colors = listOf(
                    WhatsAppLightGreen,
                    Color(0xFF55EAA0),
                    Color(0xFFFFFFFF)
                )
            )

            val rLeft = w * 0.26f
            val rTop = h * 0.34f
            val stemWidth = w * 0.15f
            val rHeight = h * 0.58f
            val rBottom = rTop + rHeight

            // Vertical stem of R
            drawRoundRect(
                brush = letterBrush,
                topLeft = Offset(rLeft, rTop),
                size = Size(stemWidth, rHeight),
                cornerRadius = CornerRadius(stemWidth * 0.3f, stemWidth * 0.3f)
            )

            // Loop of R
            val loopWidth = w * 0.40f
            val loopHeight = rHeight * 0.56f
            val loopRectTop = rTop
            val loopRectLeft = rLeft + stemWidth * 0.5f

            val rLoopPath = Path().apply {
                moveTo(rLeft, loopRectTop)
                lineTo(loopRectLeft + loopWidth * 0.45f, loopRectTop)
                cubicTo(
                    loopRectLeft + loopWidth * 1.05f, loopRectTop,
                    loopRectLeft + loopWidth * 1.05f, loopRectTop + loopHeight,
                    loopRectLeft + loopWidth * 0.45f, loopRectTop + loopHeight
                )
                lineTo(rLeft, loopRectTop + loopHeight)
                close()
            }
            drawPath(path = rLoopPath, brush = letterBrush, style = Fill)

            // Inner cutout for the loop
            val innerCutoutPath = Path().apply {
                val cutLeft = rLeft + stemWidth
                val cutTop = loopRectTop + stemWidth * 0.7f
                val cutHeight = loopHeight - stemWidth * 1.4f
                val cutWidth = loopWidth * 0.48f

                moveTo(cutLeft, cutTop)
                lineTo(cutLeft + cutWidth * 0.4f, cutTop)
                cubicTo(
                    cutLeft + cutWidth * 0.9f, cutTop,
                    cutLeft + cutWidth * 0.9f, cutTop + cutHeight,
                    cutLeft + cutWidth * 0.4f, cutTop + cutHeight
                )
                lineTo(cutLeft, cutTop + cutHeight)
                close()
            }
            drawPath(path = innerCutoutPath, color = WhatsAppDarkGreen, style = Fill)

            // Diagonal leg of R
            val legPath = Path().apply {
                moveTo(rLeft + stemWidth * 0.8f, loopRectTop + loopHeight * 0.85f)
                lineTo(rLeft + stemWidth * 1.7f, loopRectTop + loopHeight * 0.85f)
                lineTo(rLeft + loopWidth + stemWidth * 0.4f, rBottom)
                lineTo(rLeft + loopWidth - stemWidth * 0.4f, rBottom)
                close()
            }
            drawPath(path = legPath, brush = letterBrush, style = Fill)

            // Subtle green glowing dot (AI status light)
            drawCircle(
                color = WhatsAppLightGreen,
                radius = w * 0.05f,
                center = Offset(w * 0.82f, h * 0.85f)
            )
        }
    }
}
