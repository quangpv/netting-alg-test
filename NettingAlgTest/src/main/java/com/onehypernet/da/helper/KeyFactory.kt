package com.onehypernet.da.helper

//import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
//import org.bouncycastle.jce.provider.BouncyCastleProvider
//import org.bouncycastle.openssl.PEMEncryptedKeyPair
//import org.bouncycastle.openssl.PEMKeyPair
//import org.bouncycastle.openssl.PEMParser
//import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
//import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
//import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
//import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo

import com.onehypernet.da.extensions.base64ToByteArray
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


interface KeyFactory {
    fun isEncryptedKey(privateKeyFile: ByteArray): Boolean {
        return isEncryptedKey(String(privateKeyFile))
    }

    fun isEncryptedKey(privateKey: String): Boolean {
        return privateKey.contains("BEGIN ENCRYPTED PRIVATE KEY")
    }

    fun createPrivate(privateKey: ByteArray, password: String = ""): PrivateKey

    fun createPrivate(privateKey: String, password: String = ""): PrivateKey {
        return createPrivate(privateKey.toByteArray(), password)
    }

    fun createCert(certBase64: String): Certificate {
        return createCert(certBase64.base64ToByteArray())
    }

    fun createCert(bytes: ByteArray): Certificate

    companion object : KeyFactory by DefaultKeyFactory()
}

//class BouncyCastleKeyFactory : KeyFactory {
//    override fun createPrivate(privateKey: InputStream, password: String): PrivateKey {
//        val parser = PEMParser(InputStreamReader(privateKey))
//        val `object` = parser.readObject() ?: throw IllegalArgumentException("Can not parse to private key")
//        val provider = BouncyCastleProvider()
//        val converter = JcaPEMKeyConverter().setProvider(provider)
//
//        return when (`object`) {
//            is PEMEncryptedKeyPair -> {
//                // PKCS1 encrypted key
//                val decryptionProvider =
//                    JcePEMDecryptorProviderBuilder().setProvider(provider).build(password.toCharArray())
//                val keypair = `object`.decryptKeyPair(decryptionProvider)
//                converter.getPrivateKey(keypair.privateKeyInfo)
//            }
//            is PKCS8EncryptedPrivateKeyInfo -> {
//                // PKCS8 encrypted key
//                val decryptionProvider =
//                    JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider(provider).build(password.toCharArray())
//                val info = `object`.decryptPrivateKeyInfo(decryptionProvider)
//                converter.getPrivateKey(info)
//            }
//            is PEMKeyPair -> {
//                // PKCS1 unencrypted key
//                converter.getKeyPair(`object`).private
//            }
//            is PrivateKeyInfo -> {
//                // PKCS8 unencrypted key
//                converter.getPrivateKey(`object`)
//            }
//            else -> throw UnsupportedOperationException("Unsupported PEM object: " + `object`.javaClass.simpleName)
//        }
//    }
//
//    override fun createCert(cert: InputStream): Certificate {
//        try {
//            return Certificate(CertificateFactory().engineGenerateCertificate(cert) as X509Certificate)
//        } catch (e: Throwable) {
//            error(e.message ?: "Certificate is not valid")
//        }
//    }
//}

class DefaultKeyFactory : KeyFactory {
    override fun createPrivate(privateKey: ByteArray, password: String): PrivateKey {
        val key = String(privateKey)
        if (!isEncryptedKey(privateKey)) return createPrivate(key)
        return createEncryptedPrivate(key, password)
    }

    private fun createPrivate(privateKey: String): PrivateKey {
        val privateKeyPEM = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END PRIVATE KEY-----", "")

        val encoded = Base64.getDecoder().decode(privateKeyPEM)

        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }

    private fun createEncryptedPrivate(privateKey: String, password: String): PrivateKey {
        val b64 = privateKey.replace("-----(BEGIN|END) ENCRYPTED PRIVATE KEY-----|\r?\n".toRegex(), "")
        val der = Base64.getDecoder().decode(b64)

        val pbeSpec = PBEKeySpec(password.toCharArray())
        val pkinfo = EncryptedPrivateKeyInfo(der)
        val skf = SecretKeyFactory.getInstance(pkinfo.algName)
        val secret = skf.generateSecret(pbeSpec)
        val keySpec: PKCS8EncodedKeySpec = pkinfo.getKeySpec(secret)
        val kf = java.security.KeyFactory.getInstance("RSA")
        return kf.generatePrivate(keySpec)
    }

    override fun createCert(bytes: ByteArray): Certificate {
        try {
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(bytes.inputStream()) as X509Certificate
            return Certificate(cert)
        } catch (e: Throwable) {
            error(e.message ?: "Certificate is not valid")
        }
    }
}