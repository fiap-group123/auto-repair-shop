package br.com.autorepairshop.authentication.application.security

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.Role
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith

@Tag("unit")
class AccessGuardTest {
    private val actors = mockk<ActorProvider>()
    private val guard = AccessGuard(actors = actors)

    @Test
    fun `staff can access any customer`() {
        every { actors.current() } returns Actor(
            userId = UUID.randomUUID(),
            role = Role.MANAGER,
            customerId = null,
        )

        guard.requireCustomer(customerId = UUID.randomUUID())
    }

    @Test
    fun `client cannot access another customer`() {
        val ownId = UUID.randomUUID()
        every { actors.current() } returns Actor(
            userId = UUID.randomUUID(),
            role = Role.CLIENT,
            customerId = ownId,
        )

        assertFailsWith<AuthenticationException.Forbidden> {
            guard.requireCustomer(customerId = UUID.randomUUID())
        }
    }

    @Test
    fun `missing actor is unauthenticated`() {
        every { actors.current() } returns null

        assertFailsWith<AuthenticationException.Unauthenticated> {
            guard.requireCustomer(customerId = UUID.randomUUID())
        }
    }
}
