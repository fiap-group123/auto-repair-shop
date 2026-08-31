package br.com.autorepairshop.authentication.domain.aggregate

import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.Role
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class UserTest {

    @Test
    fun `client without customer id fails`() {
        assertFailsWith<AuthenticationException.InvalidRole> {
            User.register(
                email = LoginEmail.of(raw = AuthFixtures.CLIENT_EMAIL),
                hashedPassword = AuthFixtures.hashedPassword(),
                role = Role.CLIENT,
            )
        }
    }

    @Test
    fun `staff with customer id fails`() {
        assertFailsWith<AuthenticationException.InvalidRole> {
            User.register(
                email = LoginEmail.of(raw = AuthFixtures.MANAGER_EMAIL),
                hashedPassword = AuthFixtures.hashedPassword(),
                role = Role.MANAGER,
                customerId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `registers manager and client`() {
        val customerId = UUID.randomUUID()
        val manager = AuthFixtures.manager()
        val client = AuthFixtures.client(customerId = customerId)

        assertTrue(manager.active)
        assertNull(manager.customerId)
        assertEquals(
            expected = Role.MANAGER,
            actual = manager.role,
        )
        assertEquals(
            expected = customerId,
            actual = client.customerId,
        )
        assertEquals(
            expected = Role.CLIENT,
            actual = client.role,
        )
    }

    @Test
    fun `deactivate and reactivate follow customer invariants`() {
        val user = AuthFixtures.manager()
        user.deactivate()
        assertFailsWith<AuthenticationException.UserInactive> {
            user.deactivate()
        }
        user.reactivate()
        assertTrue(user.active)
        assertFailsWith<AuthenticationException.UserAlreadyActive> {
            user.reactivate()
        }
    }
}
