package io.github.ianpogi5.nightstand.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.ianpogi5.nightstand.Prefs
import io.github.ianpogi5.nightstand.R
import io.github.ianpogi5.nightstand.calendar.Calendars
import io.github.ianpogi5.nightstand.trigger.ChargingTriggerService

@Composable
fun SetupSheet(
    hasCalendarPermission: Boolean,
    onSettingsChanged: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var autoLaunch by remember { mutableStateOf(Prefs.autoLaunchEnabled(context)) }
    var landscapeOnly by remember { mutableStateOf(Prefs.landscapeOnly(context)) }
    var canOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var clockStyle by remember { mutableStateOf(Prefs.clockStyle(context)) }
    var calendarStyle by remember { mutableStateOf(Prefs.calendarStyle(context)) }
    var nightBrightness by remember { mutableStateOf(Prefs.nightBrightness(context)) }
    var hiddenCalendars by remember { mutableStateOf(Prefs.hiddenCalendars(context)) }
    val calendars = remember {
        if (hasCalendarPermission) Calendars.query(context) else emptyList()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canOverlay = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    fun setAutoLaunch(enabled: Boolean) {
        autoLaunch = enabled
        Prefs.setAutoLaunchEnabled(context, enabled)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= 33) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            ChargingTriggerService.start(context)
            if (!canOverlay) context.openOverlaySettings()
        } else {
            ChargingTriggerService.stop(context)
        }
    }

    Surface(
        color = Color(0xFF0F0F14),
        contentColor = Color(0xFFEDEDED),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
        ) {
            // Fullscreen Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Einstellungen",
                        fontSize = 26.sp,
                        fontFamily = SFProFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Nightstand • iOS StandBy Display",
                        fontSize = 13.sp,
                        fontFamily = SFProFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8E8E93),
                    )
                }

                FilledTonalButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Fertig",
                        fontFamily = SFProFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }

            // Scrollable Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Left Column: Display & Styles
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SettingsCard(title = "Uhren-Stil") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            StyleChoice(
                                label = "Digital",
                                selected = clockStyle == Prefs.CLOCK_DIGITAL,
                                modifier = Modifier.weight(1f),
                            ) {
                                clockStyle = Prefs.CLOCK_DIGITAL
                                Prefs.setClockStyle(context, clockStyle)
                                onSettingsChanged()
                            }
                            StyleChoice(
                                label = "Analog",
                                selected = clockStyle == Prefs.CLOCK_ANALOG,
                                modifier = Modifier.weight(1f),
                            ) {
                                clockStyle = Prefs.CLOCK_ANALOG
                                Prefs.setClockStyle(context, clockStyle)
                                onSettingsChanged()
                            }
                            StyleChoice(
                                label = "Float (Bunt)",
                                selected = clockStyle == Prefs.CLOCK_FLOAT,
                                modifier = Modifier.weight(1.2f),
                            ) {
                                clockStyle = Prefs.CLOCK_FLOAT
                                Prefs.setClockStyle(context, clockStyle)
                                onSettingsChanged()
                            }
                        }
                    }

                    SettingsCard(title = "Kalender-Ansicht") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            StyleChoice(
                                label = "Monat (iOS)",
                                selected = calendarStyle == Prefs.CALENDAR_MONTH,
                                modifier = Modifier.weight(1f),
                            ) {
                                calendarStyle = Prefs.CALENDAR_MONTH
                                Prefs.setCalendarStyle(context, calendarStyle)
                                onSettingsChanged()
                            }
                            StyleChoice(
                                label = "Termine",
                                selected = calendarStyle == Prefs.CALENDAR_LIST,
                                modifier = Modifier.weight(1f),
                            ) {
                                calendarStyle = Prefs.CALENDAR_LIST
                                Prefs.setCalendarStyle(context, calendarStyle)
                                onSettingsChanged()
                            }
                            StyleChoice(
                                label = "Aus",
                                selected = calendarStyle == Prefs.CALENDAR_NONE,
                                modifier = Modifier.weight(0.7f),
                            ) {
                                calendarStyle = Prefs.CALENDAR_NONE
                                Prefs.setCalendarStyle(context, calendarStyle)
                                onSettingsChanged()
                            }
                        }
                    }

                    SettingsCard(title = "Nachtmodus-Helligkeit") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Dimmstufe",
                                    fontSize = 14.sp,
                                    fontFamily = SFProFontFamily,
                                    color = Color(0xFFAAAAAA),
                                )
                                Text(
                                    text = "${(nightBrightness * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontFamily = SFProFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF),
                                )
                            }
                            Slider(
                                value = nightBrightness,
                                onValueChange = { nightBrightness = it },
                                onValueChangeFinished = {
                                    Prefs.setNightBrightness(context, nightBrightness)
                                    onSettingsChanged()
                                },
                                valueRange = 0.01f..0.4f,
                            )
                        }
                    }
                }

                // Right Column: System & Triggers
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SettingsCard(title = "Verhalten beim Laden") {
                        SwitchRow(
                            label = "Automatisch beim Laden starten",
                            checked = autoLaunch,
                            onCheckedChange = ::setAutoLaunch,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SwitchRow(
                            label = "Nur im Querformat aktivieren",
                            checked = landscapeOnly,
                            onCheckedChange = {
                                landscapeOnly = it
                                Prefs.setLandscapeOnly(context, it)
                            },
                        )
                    }

                    SettingsCard(title = "Systemberechtigungen") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Über anderen Apps anzeigen",
                                fontSize = 15.sp,
                                fontFamily = SFProFontFamily,
                                modifier = Modifier.weight(1f),
                            )
                            if (canOverlay) {
                                Text(
                                    text = "Erteilt ✓",
                                    fontSize = 14.sp,
                                    fontFamily = SFProFontFamily,
                                    color = Color(0xFF34C759),
                                    fontWeight = FontWeight.Bold,
                                )
                            } else {
                                TextButton(onClick = { context.openOverlaySettings() }) {
                                    Text(
                                        text = "Erlauben",
                                        fontFamily = SFProFontFamily,
                                        color = Color(0xFF007AFF),
                                    )
                                }
                            }
                        }
                    }

                    if (calendars.isNotEmpty()) {
                        SettingsCard(title = "Sichtbare Kalender") {
                            calendars.forEach { calendar ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(calendar.color), CircleShape),
                                    )
                                    Text(
                                        text = calendar.name,
                                        fontSize = 14.sp,
                                        fontFamily = SFProFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp),
                                    )
                                    Checkbox(
                                        checked = calendar.id !in hiddenCalendars,
                                        onCheckedChange = { shown ->
                                            hiddenCalendars = if (shown) {
                                                hiddenCalendars - calendar.id
                                            } else {
                                                hiddenCalendars + calendar.id
                                            }
                                            Prefs.setHiddenCalendars(context, hiddenCalendars)
                                            onSettingsChanged()
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { context.openAppInfo() }) {
                            Text(
                                text = "App-Informationen öffnen",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                fontFamily = SFProFontFamily,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C22),
            contentColor = Color(0xFFEDEDED),
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontFamily = SFProFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun StyleChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) Color(0xFF007AFF) else Color(0xFF2C2C34),
            contentColor = if (selected) Color.White else Color(0xFFBBBBBB),
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = SFProFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = SFProFontFamily,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun android.content.Context.openOverlaySettings() {
    startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        ),
    )
}

private fun android.content.Context.openAppInfo() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName"),
        ),
    )
}
