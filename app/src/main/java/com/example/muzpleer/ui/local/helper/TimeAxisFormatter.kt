package com.example.muzpleer.ui.local.helper

import com.github.mikephil.charting.formatter.ValueFormatter

class TimeAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val seconds = value.toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}