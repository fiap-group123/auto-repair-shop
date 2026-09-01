package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.application.security.Actor
import br.com.autorepairshop.accessidentity.application.security.ActorProvider
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class CurrentUserTest {
    private val actors = mockk<ActorProvider>()
    private val currentUser = CurrentUser(actors = actors)

    @Test
    fun `maps the authenticated actor`() {
        val userId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        every { actors.current() } returns Actor(
            userId = userId,
            role = Role.CLIENT,
            customerId = customerId,
        )

        val user = currentUser.get()

        assertEquals(expected = userId, actual = user.userId)
        assertEquals(expected = Role.CLIENT, actual = user.role)
        assertEquals(expected = customerId, actual = user.customerId)
    }

    @Test
    fun `maps staff without a customer id`() {
        val userId = UUID.randomUUID()
        every { actors.current() } returns Actor(
            userId = userId,
            role = Role.MANAGER,
            customerId = null,
        )

        val user = currentUser.get()

        assertEquals(expected = userId, actual = user.userId)
        assertEquals(expected = Role.MANAGER, actual = user.role)
        assertNull(actual = user.customerId)
    }

    @Test
    fun `rejects a missing actor`() {
        every { actors.current() } returns null

        assertThrows<AuthenticationException.Unauthenticated> { currentUser.get() }
    }
}
