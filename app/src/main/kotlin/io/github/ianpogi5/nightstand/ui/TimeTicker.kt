package io.github.ianpogi5.nightstand.ui

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import java.time.LocalDateTime
import kotlinx.coroutines.delay

/**
 * Current wall-clock time. Updates every second when [withSeconds] is true
 * (e.g. for the analog clock's second hand), or on each minute boundary
 * for the digital clock.
 */
@Composable
fun rememberMinuteTime(withSeconds: Boolean = false): State<LocalDateTime> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val time = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(lifecycleOwner, withSeconds) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                time.value = LocalDateTime.now()
                val interval = if (withSeconds) 1000L else 60_000L
                val nextDelay = interval - (System.currentTimeMillis() % interval)
                delay(if (nextDelay > 0) nextDelay else interval)
            }
        }
    }
    return time
}

@Composable
fun rememberIs24HourFormat(): Boolean {
    val context = LocalContext.current
    return remember(context) { DateFormat.is24HourFormat(context) }
}
