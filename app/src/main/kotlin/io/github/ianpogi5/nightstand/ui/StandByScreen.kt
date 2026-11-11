package io.github.ianpogi5.nightstand.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ianpogi5.nightstand.Prefs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/** Max burn-in shift in dp, applied in each axis every minute. */
private const val SHIFT_RANGE_DP = 8

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

@Composable
fun StandByScreen(
    dimmed: Boolean,
    onToggleDim: () -> Unit,
    hasCalendarPermission: Boolean,
    clockStyle: String,
    calendarStyle: String,
    hiddenCalendars: Set<Long>,
    onOpenSetup: () -> Unit,
) {
    val isAnalog = (clockStyle == Prefs.CLOCK_ANALOG)
    val now by rememberMinuteTime(withSeconds = isAnalog)
    val battery = rememberBatteryStatus()
    val events = rememberTodayEvents(now, hasCalendarPermission, hiddenCalendars)

    // Fresh pseudo-random offset each minute so no pixel stays lit in place.
    val shift = remember(now.hour to now.minute) {
        IntOffset(
            Random.nextInt(-SHIFT_RANGE_DP, SHIFT_RANGE_DP + 1),
            Random.nextInt(-SHIFT_RANGE_DP, SHIFT_RANGE_DP + 1),
        )
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val contentColor = if (dimmed) Color(0xFF8A8A8A) else Color(0xFFEDEDED)
    val secondaryColor = if (dimmed) Color(0xFF565656) else Color(0xFF9A9A9A)

    val hasCalendar = (calendarStyle == Prefs.CALENDAR_MONTH || (calendarStyle == Prefs.CALENDAR_LIST && events.isNotEmpty()))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleDim,
            ),
    ) {
        val clockContent: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (clockStyle) {
                    Prefs.CLOCK_ANALOG -> {
                        val analogSize = if (isPortrait) {
                            if (hasCalendar) 260.dp else 330.dp
                        } else {
                            if (hasCalendar) 290.dp else 330.dp
                        }
                        AnalogClock(
                            time = now,
                            color = contentColor,
                            secondaryColor = secondaryColor,
                            dimmed = dimmed,
                            size = analogSize,
                        )
                    }
                    Prefs.CLOCK_FLOAT -> {
                        FloatClock(
                            time = now,
                            dimmed = dimmed,
                            isSideBySide = hasCalendar,
                            isPortrait = isPortrait,
                        )
                    }
                    else -> {
                        Clock(
                            time = now,
                            color = contentColor,
                            isFullscreen = !hasCalendar,
                            isPortrait = isPortrait,
                        )
                    }
                }
                if (clockStyle != Prefs.CLOCK_FLOAT || hasCalendar) {
                    Spacer(modifier = Modifier.height(if (hasCalendar) 10.dp else 12.dp))
                    Text(
                        text = now.toLocalDate().format(dateFormatter),
                        color = secondaryColor,
                        fontSize = if (hasCalendar) 16.sp else 19.sp,
                        fontFamily = SFProFontFamily,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }

        val calendarContent: @Composable () -> Unit = {
            if (calendarStyle == Prefs.CALENDAR_MONTH) {
                MonthCalendar(
                    today = now.toLocalDate(),
                    events = events,
                    dimmed = dimmed,
                )
            } else if (calendarStyle == Prefs.CALENDAR_LIST && events.isNotEmpty()) {
                CalendarColumn(
                    events = events,
                    dimmed = dimmed,
                    modifier = Modifier.widthIn(max = 320.dp),
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(shift.x.dp.roundToPx(), shift.y.dp.roundToPx()) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                clockContent()
                if (hasCalendar) {
                    Spacer(modifier = Modifier.height(28.dp))
                    calendarContent()
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(shift.x.dp.roundToPx(), shift.y.dp.roundToPx()) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                clockContent()
                if (hasCalendar) {
                    val spacerWidth = when (clockStyle) {
                        Prefs.CLOCK_DIGITAL -> 70.dp
                        Prefs.CLOCK_FLOAT -> 80.dp
                        else -> 90.dp
                    }
                    Spacer(modifier = Modifier.width(spacerWidth))
                    calendarContent()
                }
            }
        }

        BatteryIndicator(
            status = battery,
            dimmed = dimmed,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (isPortrait) 36.dp else 24.dp, end = if (isPortrait) 20.dp else 28.dp)
                .offset { IntOffset(shift.x.dp.roundToPx(), shift.y.dp.roundToPx()) },
        )

        Text(
            text = "⚙",
            color = secondaryColor.copy(alpha = 0.6f),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(if (isPortrait) 20.dp else 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSetup,
                )
                .padding(8.dp),
        )
    }
}

@Composable
private fun Clock(
    time: LocalDateTime,
    color: Color,
    isFullscreen: Boolean = false,
    isPortrait: Boolean = false,
) {
    val pattern = if (rememberIs24HourFormat()) "HH:mm" else "h:mm"
    val fontSize = if (isPortrait) {
        if (isFullscreen) 160.sp else 125.sp
    } else {
        if (isFullscreen) 260.sp else 155.sp
    }
    Text(
        text = time.format(DateTimeFormatter.ofPattern(pattern)),
        color = color,
        fontSize = fontSize,
        fontFamily = SFProFontFamily,
        fontWeight = FontWeight.Bold,
    )
}
