package com.onehypernet.da.helper

import java.text.DecimalFormat

class TextFormatter {
    fun formatFee(price: Double): String {
        return DecimalFormat("###,###,#00.00").format(price)
    }

    fun formatFee(price: Double, suffix: String): String {
        return "${DecimalFormat("###,###,#00.00").format(price)} $suffix"
    }

    fun formatAmount(price: Double): String {
        return DecimalFormat("###,###,###,###,#00.##").format(price)
    }

    fun formatRate(rate: Double): String {
        return "$rate"
    }

    fun formatPercent(percent: Double): String {
        return "${DecimalFormat("##,##").format(percent)}%"
    }

    fun formatBoolean(convertibility: Int): String {
        return if (convertibility != 0) "Y" else "N"
    }

    fun formatBoolean(b: String): String {
        if (b == "Y" || b == "N") return b
        return if (b.toBoolean()) "Y" else "N"
    }
}

val textFormatter = TextFormatter()