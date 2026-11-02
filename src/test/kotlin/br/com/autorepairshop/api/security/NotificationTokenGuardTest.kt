package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@Tag("unit")
class NotificationTokenGuardTest {
    private val guard = NotificationTokenGuard(expectedToken = "local-budget-notification-token")

    @Test
    fun `accepts the configured token`() {
        guard.require(presented = "local-budget-notification-token")
    }

    @Test
    fun `rejects a missing or different token`() {
        assertFailsWith<AuthenticationException.Unauthenticated> {
            guard.require(presented = null)
        }
        assertFailsWith<AuthenticationException.Unauthenticated> {
            guard.require(presented = "other-token")
        }
    }

    @Test
    fun `rejects every token when the secret is blank`() {
        val open = NotificationTokenGuard(expectedToken = "")
        assertFailsWith<AuthenticationException.Unauthenticated> {
            open.require(presented = "")
        }
    }
}
