@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.onehypernet.da.helper

import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings
import java.io.File
import java.io.InputStream
import java.net.URL

object CSVLoader {

    inline fun <reified T> load(file: File): List<T> = load(file.inputStream())

    inline fun <reified T> load(file: InputStream): List<T> {
        return load(file, T::class.java)
    }

    fun <T> load(file: InputStream, clazz: Class<T>): List<T> {
        val csv = CsvParser(CsvParserSettings())
        return csv.parseAll(file).map {
            createObject(it, clazz)
        }
    }

    inline fun <reified T> load(path: URL?): List<T> {
        return load(path!!.openStream())
    }

    fun <T> load(path: File, factory: (Array<String>) -> T): List<T> = load(path.inputStream(), factory)

    fun <T> load(path: InputStream, factory: (Array<String>) -> T): List<T> {
        val csv = CsvParser(CsvParserSettings())
        return csv.parseAll(path).map(factory)
    }

    fun <T> load(path: URL?, factory: (Array<String>) -> T): List<T> {
        return load(path!!.openStream(), factory)
    }

    @Suppress("unchecked_cast")
    fun <T> createObject(args: Array<String>, clazz: Class<out T>): T {
        val constructors = clazz.declaredConstructors
        if (constructors.isEmpty()) throw Throwable("Not found constructor of ${clazz.name}")
        val constructor = constructors.find { it.parameterCount == args.size }
            ?: throw Throwable("Num of parameter ${clazz.name} should equals ${args.size}")

        val values = constructor.parameterTypes.mapIndexed { index, paramClazz ->
            when {
                paramClazz.isAssignableFrom(Int::class.java) -> args[index].toInt()
                paramClazz.isAssignableFrom(String::class.java) -> args[index]
                paramClazz.isAssignableFrom(Double::class.java) -> args[index].toDouble()
                paramClazz.isAssignableFrom(Float::class.java) -> args[index].toFloat()
                paramClazz.isAssignableFrom(Long::class.java) -> args[index].toLong()
                else -> error("Not support type ${paramClazz.javaClass.name}")
            }
        }
        return constructor.newInstance(*values.toTypedArray()) as T
    }
}