package com.onehypernet.da.extensions


fun ByteArray?.safe(): ByteArray {
    return ByteArray(0)
}

fun String?.safe(def: String = ""): String {
    if (this == null) return ""
    if (this.isBlank()) return def
    return this
}

val String.orNA: String
    get() = ifBlank { "N/A" }


fun Boolean?.safe(def: Boolean = false): Boolean = this ?: def
fun <T> List<T>?.safe() = this ?: emptyList()
