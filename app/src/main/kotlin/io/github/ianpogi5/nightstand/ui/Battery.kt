package io.github.ianpogi5.nightstand.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BatteryStatus(val percent: Int?, val charging: Boolean)

@Composable
fun rememberBatteryStatus(): BatteryStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(BatteryStatus(percent = null, charging = false)) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                status = intent.toBatteryStatus()
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        sticky?.let { status = it.toBatteryStatus() }
        onDispose { context.unregisterReceiver(receiver) }
    }
    return status
}

private fun Intent.toBatteryStatus(): BatteryStatus {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val state = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    return BatteryStatus(
        percent = if (level >= 0 && scale > 0) level * 100 / scale else null,
        charging = state == BatteryManager.BATTERY_STATUS_CHARGING ||
            state == BatteryManager.BATTERY_STATUS_FULL,
    )
}

/**
 * iOS 17 StandBy-style circular battery widget:
 * - Circular progress ring
 * - Centered number only (no percent sign)
 */
@Composable
fun BatteryIndicator(
    status: BatteryStatus,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    val percent = status.percent ?: return
    val ringColor = if (dimmed) Color(0xFF1E6E34) else Color(0xFF34C759)
    val trackColor = if (dimmed) Color(0xFF1B2A1E) else Color(0x3334C759)
    val textColor = if (dimmed) Color(0xFF6E9E78) else Color(0xFF34C759)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 3.5.dp.toPx()
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background circle track
            drawCircle(
                color = trackColor,
                radius = (this.size.width - strokeWidth) / 2f,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            // Progress arc
            val sweepAngle = (percent / 100f).coerceIn(0f, 1f) * 360f
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Text(
            text = "$percent",
            color = textColor,
            fontSize = 17.sp,
            fontFamily = SFProFontFamily,
            fontWeight = FontWeight.Bold,
        )
    }
}
