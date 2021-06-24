package com.onehypernet.da.helper

import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.io.File

class HttpClient(private val url: String) {
    private val client = HttpClients.createDefault()

    private fun url(path: String): String {
        return "$url/$path".trimEnd('/')
    }

    fun post(path: String, function: PostRequest.() -> Unit = {}): ByteArray {
        return client.execute(HttpPost(url(path)).apply {
            PostRequest(this).apply(function).build()
        }).entity.content.readBytes()
    }

    fun get(path: String): ByteArray {
        return client.execute(HttpGet(url(path))).entity.content.readBytes()
    }

    class PostRequest(private val httpPost: HttpPost) {
        private var mParams: Map<String, String> = emptyMap()
        private var mHeaders: Map<String, String> = emptyMap()
        private var mFile: File? = null
        private var mBody: ByteArray? = null

        fun body(body: ByteArray) {
            mBody = body
        }

        fun file(file: File) {
            mFile = file
        }

        fun headers(headers: Map<String, String>) {
            mHeaders = headers
        }

        fun params(keyValue: Map<String, String>) {
            mParams = keyValue
        }

        fun build() {
            for (mHeader in mHeaders) {
                httpPost.addHeader(mHeader.key, mHeader.value)
            }
            httpPost.entity = EntityBuilder.create().apply {
                if (mBody != null) binary = mBody
                if (mFile != null) file = mFile
                parameters = mParams.map { BasicNameValuePair(it.key, it.value) }
            }.build()
        }
    }
}