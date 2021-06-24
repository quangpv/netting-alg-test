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

    fun formatConvertible(convertibility: Int): String {
        return if (convertibility != 0) "Y" else "N"
    }
}

val textFormatter = TextFormatter()