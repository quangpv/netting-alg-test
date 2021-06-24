package com.onehypernet.da.core

import com.google.gson.Gson

interface Parser {
    fun <T> fromJson(string: String?, type: Class<T>): T?
    fun <T> toJson(value: T?): String
}

class GsonParser : Parser {
    override fun <T> fromJson(string: String?, type: Class<T>): T? {
        return Gson().fromJson(string, type)
    }

    override fun <T> toJson(value: T?): String {
        return Gson().toJson(value)
    }
}