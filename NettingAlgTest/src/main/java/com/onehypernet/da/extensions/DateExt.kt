package com.onehypernet.da.extensions

import java.text.SimpleDateFormat
import java.util.*


fun Date.format(format: String): String {
    return SimpleDateFormat(format).format(this)
}

fun Date.isDayEquals(now: Date): Boolean {
    return Calendar.getInstance().run {
        time = this@isDayEquals
        val dayOfYear = get(Calendar.DAY_OF_YEAR)
        time = now
        dayOfYear == get(Calendar.DAY_OF_YEAR)
    }
}

fun Date.isDayEquals(now: Long): Boolean {
    return Calendar.getInstance().run {
        time = this@isDayEquals
        val dayOfYear = get(Calendar.DAY_OF_YEAR)
        timeInMillis = now
        dayOfYear == get(Calendar.DAY_OF_YEAR)
    }
}