package com.onehypernet.da.extensions


import java.util.*

fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

fun String.base64ToRealString() = String(base64ToByteArray())

fun String.base64ToByteArray(): ByteArray = Base64.getDecoder().decode(this)



