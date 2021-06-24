package com.onehypernet.da.core

object Log {
    fun e(tag: String, message: String) {
        System.err.println("$tag:\t$message")
    }

    fun d(tag: String, message: String) {
        println("$tag:\t$message")
    }

    fun e(tag: String, message: Throwable) {
        println("$tag:\r${message.message}")
        message.printStackTrace(System.out)
    }
}