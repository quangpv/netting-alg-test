package com.onehypernet.da.app

import kotlin.streams.asSequence

abstract class Resource(fileName: String, private val prefix: String = "") {
    private var attrs = hashMapOf<String, String>()

    init {
        javaClass.getResourceAsStream("/css/$fileName.css")!!.bufferedReader().lines().asSequence()
            .map { it.trimStart().trimEnd(';') }
            .filter {
                it.startsWith("-fx-")
            }.map {
                if (prefix.isNotBlank()) it.removePrefix("-fx-$prefix-")
                else it
            }.forEach {
                val (k, v) = it.split(":")
                attrs[k.trim()] = v.trim()
            }
    }

    fun valueOf(key: String) = attrs[key]!!
}