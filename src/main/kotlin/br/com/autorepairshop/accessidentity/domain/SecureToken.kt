package br.com.autorepairshop.accessidentity.domain

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale

object SecureToken {
    private const val BYTE_SIZE = 32

    fun generate(): String {
        val bytes = ByteArray(size = BYTE_SIZE)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hash(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(Locale.ROOT, it) }
    }
}
