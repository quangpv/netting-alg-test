package com.onehypernet.da.app

import javafx.scene.paint.Color

object R {
    val string: StringResource = StringResource()
    val color: ColorResource = ColorResource()
    val theme: ThemeResource = ThemeResource()
}

class ThemeResource {
    val dragEnter = "drag-drop-enter"
    val boxBorderOpacity = "box-border-primary-light"
    val boxBorderGreyLight = "box-border-grey-light"
}

class ColorResource : Resource("colors", "color") {
    val grey = valueOf("grey")
    val primary = valueOf("primary")
    val black = valueOf("black")
    val white = valueOf("white")
    val greyLight = valueOf("grey-light")

    fun value(f: ColorResource.() -> String): Color {
        return Color.valueOf(f(this))
    }
}

class StringResource {
    val to = "To"
    val from = "From"
    val token = "Token"
    val usd = "US$"
    val percent = "%"
    val no = "No."
    val savings = "Savings (%s)"
    val postNetting = "Post Netting (%s)"
    val preNetting = "Pre Netting (%s)"

    val type = "Type"
    val entity = "Entity"
    val feeInUSD = "USD"
    val exchangeRate = "FX"
    val feeFixed = "Fee (USD)"
    val feeRate = "%"
    val fx1 = "FX1"
    val fx2 = "FX2"
    val notAvailable = "(unavailabe)"
    val location = "Location"
    val swift = "SWIFT"
    val accountNo = "Account No"
    val bankName = "Bank"
    val currency = "Currency"
    val counterPartyId = "Counterparty ID"
    val convertibility = "Convertibility"
    val amount = "Amount"
    val fx = "FX"
    val date = "Date"
    val transactionId = "Transaction ID"
}