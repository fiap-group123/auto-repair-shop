package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class LoginEmailTest {

    @Test
    fun `trims and lowercases a valid address`() {
        val email = LoginEmail.of(raw = "  Gerente@Oficina.com  ")

        assertEquals(
            expected = AuthFixtures.MANAGER_EMAIL,
            actual = email.value,
        )
    }

    @Test
    fun `rejects short and malformed addresses`() {
        assertFailsWith<AuthenticationException.InvalidEmail> {
            LoginEmail.of(raw = "a@b")
        }
        assertFailsWith<AuthenticationException.InvalidEmail> {
            LoginEmail.of(raw = "gerente.oficina.com")
        }
    }

    @Test
    fun `accepts address of 60 characters and rejects 61`() {
        val sixty = "${"a".repeat(n = 55)}@x.co"
        val email = LoginEmail.of(raw = sixty)

        assertEquals(
            expected = 60,
            actual = email.value.length,
        )
        assertEquals(
            expected = sixty,
            actual = email.value,
        )
        assertFailsWith<AuthenticationException.InvalidEmail> {
            LoginEmail.of(raw = "${"a".repeat(n = 56)}@x.co")
        }
    }
}
