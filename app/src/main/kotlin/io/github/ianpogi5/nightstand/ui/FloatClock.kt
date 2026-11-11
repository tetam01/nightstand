package io.github.ianpogi5.nightstand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * iOS 17 StandBy "Float" Clock Face:
 * - Oversized bubbly rounded numerals (SF Pro Rounded Heavy)
 * - Distinct playful pastel color gradient across digits (Pink -> Periwinkle -> Purple)
 * - Two soft, semi-transparent circular colon dots
 */
@Composable
fun FloatClock(
    time: LocalDateTime,
    dimmed: Boolean,
    isSideBySide: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val is24Hour = rememberIs24HourFormat()
    val pattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeStr = time.format(DateTimeFormatter.ofPattern(pattern))
    val parts = timeStr.split(":")
    val hourStr = parts.getOrNull(0) ?: "9"
    val minuteStr = parts.getOrNull(1) ?: "41"

    val fontSize: TextUnit = if (isSideBySide) 130.sp else 215.sp
    val dotSize: Dp = if (isSideBySide) 16.dp else 26.dp
    val dotSpacing: Dp = if (isSideBySide) 18.dp else 28.dp
    val colonPadding: Dp = if (isSideBySide) 6.dp else 12.dp

    // Color palette matching iOS 17 Float
    val pinkColor = if (dimmed) Color(0xFF6E2842) else Color(0xFFE56997)
    val coralColor = if (dimmed) Color(0xFF742E3B) else Color(0xFFF27289)
    val periwinkleColor = if (dimmed) Color(0xFF3A376E) else Color(0xFF8B88F8)
    val purpleColor = if (dimmed) Color(0xFF451454) else Color(0xFF992EB8)
    val colonColor = if (dimmed) Color(0x40666666) else Color(0x998E8E93)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Hours
        if (hourStr.length == 1) {
            FloatDigit(char = hourStr[0], color = pinkColor, fontSize = fontSize)
        } else {
            FloatDigit(char = hourStr[0], color = coralColor, fontSize = fontSize)
            FloatDigit(char = hourStr[1], color = pinkColor, fontSize = fontSize)
        }

        // Soft Translucent Colon Dots
        Column(
            verticalArrangement = Arrangement.spacedBy(dotSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = colonPadding),
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(colonColor, CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(colonColor, CircleShape),
            )
        }

        // Minutes
        val min0 = minuteStr.getOrNull(0) ?: '0'
        val min1 = minuteStr.getOrNull(1) ?: '0'
        FloatDigit(char = min0, color = periwinkleColor, fontSize = fontSize)
        FloatDigit(char = min1, color = purpleColor, fontSize = fontSize)
    }
}

@Composable
private fun FloatDigit(
    char: Char,
    color: Color,
    fontSize: TextUnit,
) {
    Text(
        text = "$char",
        color = color,
        fontSize = fontSize,
        fontFamily = SFRoundedFontFamily,
        fontWeight = FontWeight.Bold,
    )
}
