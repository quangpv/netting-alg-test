package com.onehypernet.da.helper

import com.onehypernet.da.extensions.format
import java.util.*

class TimeFormatter {
    fun formatNetting(): String {
        return "As of: ${Date().format("hh:mm a dd-MMM-yy")}"
    }

    fun MMMddyy(date: Date = Date()): String {
        return date.format("MMM dd, yy")
    }

    fun MMMddyyyy(date: Date = Date()): String {
        return date.format("MMM dd, yyyy")
    }

    fun ddMMMyyhhmma(date: Date): String {
        return date.format("dd-MMM-yy hh:mm a")
    }
}

val timeFormatter = TimeFormatter()