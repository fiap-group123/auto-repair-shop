package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class NotificationTokenGuard(@Value("\${app.notifications.budget-token}") private val expectedToken: String) {
    fun require(presented: String?) {
        val actual = presented?.encodeToByteArray() ?: ByteArray(size = 0)
        val expected = expectedToken.encodeToByteArray()
        val matches = expected.isNotEmpty() && sameToken(
            actual = actual,
            expected = expected,
        )
        if (!matches) {
            throw AuthenticationException.Unauthenticated(
                message = "Invalid notification token.",
            )
        }
    }

    @Suppress("NamedArguments")
    private fun sameToken(
        actual: ByteArray,
        expected: ByteArray,
    ): Boolean = MessageDigest.isEqual(actual, expected)
}
