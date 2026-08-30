package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class RawPasswordTest {

    @Test
    fun `accepts a letter and digit password of eight characters`() {
        assertEquals(
            expected = "senha123",
            actual = RawPassword.of(raw = "senha123").value,
        )
    }

    @Test
    fun `rejects short or letter only passwords`() {
        assertFailsWith<AuthenticationException.InvalidPassword> {
            RawPassword.of(raw = "ab12")
        }
        assertFailsWith<AuthenticationException.InvalidPassword> {
            RawPassword.of(raw = "senhasenha")
        }
        assertFailsWith<AuthenticationException.InvalidPassword> {
            RawPassword.of(raw = "12345678")
        }
    }
}
