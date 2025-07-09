package com.example.muzpleer.util

import java.util.Locale

fun Long.formatDuration(): String {
    val seconds = this / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }
}