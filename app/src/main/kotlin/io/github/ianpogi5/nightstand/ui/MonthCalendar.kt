package io.github.ianpogi5.nightstand.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ianpogi5.nightstand.calendar.EventInstance
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthCalendar(
    today: LocalDate,
    events: List<EventInstance>,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val yearMonth = YearMonth.from(today)
    val monthTitle = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()

    val accentRed = if (dimmed) Color(0xFF992222) else Color(0xFFFF3B30)
    val headerColor = if (dimmed) Color(0xFF6E6E6E) else Color(0xFF9A9A9A)
    val normalDayColor = if (dimmed) Color(0xFF7A7A7A) else Color(0xFFEDEDED)
    val otherMonthColor = if (dimmed) Color(0xFF333333) else Color(0xFF4A4A4A)

    val firstDayOfMonth = yearMonth.atDay(1)
    // Monday = 1 ... Sunday = 7
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val prevMonth = yearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(
        modifier = modifier.width(280.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // Month Header (e.g. "NOVEMBER")
        Text(
            text = monthTitle,
            color = accentRed,
            fontSize = 18.sp,
            fontFamily = SFProFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        // Day of Week Header row (M D M D F S S)
        val weekdays = listOf("M", "D", "M", "D", "F", "S", "S")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            weekdays.forEach { dayLetter ->
                Text(
                    text = dayLetter,
                    color = headerColor,
                    fontSize = 13.sp,
                    fontFamily = SFProFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Calendar Grid (up to 6 rows)
        var currentSlot = 0
        val totalSlots = 35 // 5 weeks * 7 days
        val rows = (daysInMonth + dayOfWeekOffset + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (col in 0..6) {
                    val slotIndex = row * 7 + col
                    val dayNum = slotIndex - dayOfWeekOffset + 1

                    if (dayNum in 1..daysInMonth) {
                        val isToday = dayNum == today.dayOfMonth
                        val hasEvent = events.any {
                            val eventDate = it.begin.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            eventDate.year == today.year && eventDate.monthValue == today.monthValue && eventDate.dayOfMonth == dayNum
                        }

                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(accentRed, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontFamily = SFProFontFamily,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        color = normalDayColor,
                                        fontSize = 15.sp,
                                        fontFamily = SFProFontFamily,
                                        fontWeight = FontWeight.Normal,
                                    )
                                    if (hasEvent) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .background(accentRed.copy(alpha = 0.8f), CircleShape),
                                        )
                                    }
                                }
                            }
                        }
                    } else if (dayNum <= 0) {
                        // Previous month days
                        val prevDay = daysInPrevMonth + dayNum
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$prevDay",
                                color = otherMonthColor,
                                fontSize = 14.sp,
                                fontFamily = SFProFontFamily,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    } else {
                        // Next month days
                        val nextDay = dayNum - daysInMonth
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$nextDay",
                                color = otherMonthColor,
                                fontSize = 14.sp,
                                fontFamily = SFProFontFamily,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }

        // Optional upcoming event teaser below calendar
        val nextEvent = events.firstOrNull()
        if (nextEvent != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(nextEvent.color), CircleShape),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nextEvent.title,
                    color = normalDayColor,
                    fontSize = 13.sp,
                    fontFamily = SFProFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
