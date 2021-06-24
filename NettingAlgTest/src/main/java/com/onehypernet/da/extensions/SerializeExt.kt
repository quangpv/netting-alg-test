package com.onehypernet.da.extensions

import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer


fun <T : Any> T.toBytes(): ByteArray {
    return ProtoBuf.encodeToByteArray(serializer(javaClass), this)
}

inline fun <reified T> ByteArray.decode(): T {
    if (this.isEmpty()) error("Can not decode empty bytes")
    return ProtoBuf.decodeFromByteArray(serializer(), this)
}