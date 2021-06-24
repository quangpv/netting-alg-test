@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.onehypernet.da.helper

import com.onehypernet.da.extensions.toBase64
import sun.security.provider.X509Factory
import java.security.Principal
import java.security.cert.X509Certificate


class Certificate(private val value: X509Certificate) {

    companion object {
        val LINE_SEPARATOR: String = System.getProperty("line.separator")

        operator fun get(certBase64: String) = KeyFactory.createCert(certBase64)
    }

    val issuer by lazy(LazyThreadSafetyMode.NONE) { PrincipalDecode(value.issuerDN) }
    val subject by lazy(LazyThreadSafetyMode.NONE) { PrincipalDecode(value.subjectDN) }

    val serialNumber: String get() = value.serialNumber.toString()

    val startDate get() = value.notBefore
    val endDate get() = value.notAfter

    val publicKey get() = value.publicKey

    val publicKeyContent: String
        get() = formatOutput(value.publicKey.encoded)

    val signature get() = value.signature.toBase64()

    val content: String get() = formatOutput(value.encoded)

    val commonName: String
        get() = subject.commonName

    val subjectAlternativeNames get() = value.subjectAlternativeNames?.firstOrNull()?.firstOrNull()?.toString() ?: ""

    private fun formatOutput(encoded: ByteArray): String {
        return X509Factory.BEGIN_CERT + LINE_SEPARATOR + encoded.toBase64() + LINE_SEPARATOR + X509Factory.END_CERT
    }

    class PrincipalDecode(principal: Principal) {
        private val principal = principal.name
            .split(", ")
            .associate { it.split("=").let { item -> item[0] to item[1] } }

        val commonName: String get() = principal["CN"] ?: ""
        val countryCode: String get() = principal["C"] ?: ""
        val state: String get() = principal["ST"] ?: ""
        val locality: String get() = principal["L"] ?: ""
        val organization: String get() = principal["O"] ?: ""
        val organizationUnit: String get() = principal["OU"] ?: ""
        val email: String get() = principal["EMAILADDRESS"] ?: ""
    }
}
