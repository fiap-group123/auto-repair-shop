package br.com.autorepairshop.api.security

import br.com.autorepairshop.accessidentity.application.security.Actor
import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class SecurityContextActorProviderTest {
    private val provider = SecurityContextActorProvider()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `reads the actor from the security context`() {
        val actor = Actor(
            userId = UUID.randomUUID(),
            role = Role.RECEPTIONIST,
            customerId = null,
        )
        val authentication = mockk<Authentication>()
        every { authentication.details } returns actor
        SecurityContextHolder.getContext().authentication = authentication

        assertEquals(expected = actor, actual = provider.current())
    }

    @Test
    fun `returns null when there is no authentication`() {
        assertNull(actual = provider.current())
    }

    @Test
    fun `returns null when details are not an actor`() {
        val authentication = mockk<Authentication>()
        every { authentication.details } returns "not-an-actor"
        SecurityContextHolder.getContext().authentication = authentication

        assertNull(actual = provider.current())
    }
}
