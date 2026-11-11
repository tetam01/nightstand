package io.github.ianpogi5.nightstand.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * iOS 17 StandBy-style Analog Clock Face:
 * - 60 precision tick marks (12 bold hour ticks, 48 minute ticks)
 * - 12 Arabic numerals rendered in SF Pro Bold font
 * - Distinctive rounded pill hour & minute hands
 * - Signature orange second hand with counterweight and center pivot
 */
@Composable
fun AnalogClock(
    time: LocalDateTime,
    color: Color,
    secondaryColor: Color,
    dimmed: Boolean = false,
    size: Dp = 290.dp,
) {
    val textMeasurer = rememberTextMeasurer()
    val orangeAccent = if (dimmed) Color(0xFFB35900) else Color(0xFFFF9F0A)
    val numberStyle = TextStyle(
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = color,
    )

    Canvas(modifier = Modifier.size(size)) {
        val radius = min(this.size.width, this.size.height) / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // 1. Draw 60 ticks around the outer rim
        for (i in 0 until 60) {
            val angle = Math.toRadians(i * 6.0)
            val dir = Offset(sin(angle).toFloat(), -cos(angle).toFloat())
            val isHour = (i % 5 == 0)

            val tickStart = if (isHour) radius * 0.90f else radius * 0.94f
            val tickEnd = radius * 0.99f
            val tickWidth = if (isHour) 3.5f else 1.8f
            val tickColor = if (isHour) color else secondaryColor.copy(alpha = 0.55f)

            drawLine(
                color = tickColor,
                start = center + dir * tickStart,
                end = center + dir * tickEnd,
                strokeWidth = tickWidth,
                cap = StrokeCap.Round,
            )
        }

        // 2. Draw 12 Numerals (1 to 12) in SF Pro font
        val numberRadius = radius * 0.74f
        for (hour in 1..12) {
            val angle = Math.toRadians(hour * 30.0)
            val textLayoutResult = textMeasurer.measure(
                text = "$hour",
                style = numberStyle,
            )
            val numCenter = center + Offset(
                sin(angle).toFloat() * numberRadius,
                -cos(angle).toFloat() * numberRadius,
            )
            val topLeft = Offset(
                numCenter.x - textLayoutResult.size.width / 2f,
                numCenter.y - textLayoutResult.size.height / 2f,
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = topLeft,
            )
        }

        // 3. Angles for Hands
        val second = time.second
        val minute = time.minute
        val hour = time.hour % 12

        val hourAngle = Math.toRadians((hour + minute / 60.0 + second / 3600.0) * 30.0)
        val minuteAngle = Math.toRadians((minute + second / 60.0) * 6.0)
        val secondAngle = Math.toRadians(second * 6.0)

        // Hour Hand (white rounded pill)
        val hourDir = Offset(sin(hourAngle).toFloat(), -cos(hourAngle).toFloat())
        drawLine(
            color = color,
            start = center - hourDir * (radius * 0.08f),
            end = center + hourDir * (radius * 0.52f),
            strokeWidth = 9.5f,
            cap = StrokeCap.Round,
        )

        // Minute Hand (white rounded pill)
        val minuteDir = Offset(sin(minuteAngle).toFloat(), -cos(minuteAngle).toFloat())
        drawLine(
            color = color,
            start = center - minuteDir * (radius * 0.08f),
            end = center + minuteDir * (radius * 0.80f),
            strokeWidth = 6.5f,
            cap = StrokeCap.Round,
        )

        // Center cap ring
        drawCircle(color = Color.Black, radius = 7f, center = center)
        drawCircle(color = color, radius = 5.5f, center = center)

        // 4. Second Hand (Signature iOS orange hand with counterweight)
        val secondDir = Offset(sin(secondAngle).toFloat(), -cos(secondAngle).toFloat())
        drawLine(
            color = orangeAccent,
            start = center - secondDir * (radius * 0.20f), // counterweight
            end = center + secondDir * (radius * 0.88f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round,
        )

        // Orange center pivot
        drawCircle(color = orangeAccent, radius = 4.5f, center = center)
        drawCircle(color = Color.Black, radius = 1.8f, center = center)
    }
}
