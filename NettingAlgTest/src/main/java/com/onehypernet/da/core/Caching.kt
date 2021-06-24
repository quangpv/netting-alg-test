package com.onehypernet.da.core

import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T> Preferences.reference(
    key: String = "",
    parser: Parser = GsonParser()
) = object : CacheProperty<T?> {

    private val KProperty<*>.key get() = key.ifBlank { this.name }

    private var mValue: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        if (mValue == null) {
            val stringSerialized = get(property.key, "")
            if (stringSerialized.isBlank()) return null
            mValue = parser.fromJson(stringSerialized, T::class.java)
        }
        return mValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        mValue = value
        put(property.key, parser.toJson(value))
    }
}

fun Preferences.string(def: String, prefKey: String = ""): CacheProperty<String> =
    Primitive(this, prefKey, get = { key -> get(key, def) ?: def }, set = { key, it -> put(key, it) })

fun Preferences.long(def: Long, prefKey: String = ""): CacheProperty<Long> =
    Primitive(this, prefKey, get = { key -> getLong(key, def) }, set = { key, it -> putLong(key, it) })

fun Preferences.int(def: Int, prefKey: String = ""): CacheProperty<Int> =
    Primitive(this, prefKey, get = { key -> getInt(key, def) }, set = { key, it -> putInt(key, it) })

fun Preferences.float(def: Float, prefKey: String = ""): CacheProperty<Float> =
    Primitive(this, prefKey, get = { key -> getFloat(key, def) }, set = { key, it -> putFloat(key, it) })

fun Preferences.boolean(def: Boolean, prefKey: String = ""): CacheProperty<Boolean> =
    Primitive(this, prefKey, get = { key -> getBoolean(key, def) }, set = { key, it -> putBoolean(key, it) })

fun Preferences.double(def: Double, prefKey: String = ""): CacheProperty<Double> =
    Primitive(this, prefKey, get = { key -> getDouble(key, def) }, set = { key, it -> putDouble(key, it) })

fun Preferences.byteArray(def: ByteArray, prefKey: String = ""): CacheProperty<ByteArray> =
    Primitive(this, prefKey, get = { key -> getByteArray(key, def) }, set = { key, it -> putByteArray(key, it) })

interface CacheProperty<T> : ReadWriteProperty<Any, T>

private class Primitive<T>(
    private val preferences: Preferences,
    private val prefKey: String,
    private val get: Preferences.(String) -> T,
    private val set: Preferences.(String, T) -> Unit
) : CacheProperty<T> {
    private var mValue: T? = null
    private val KProperty<*>.key get() = prefKey.ifBlank { this.name }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (mValue == null) mValue = preferences.get(property.key)
        return mValue!!
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        mValue = value
        preferences.set(property.key, value)
    }
}