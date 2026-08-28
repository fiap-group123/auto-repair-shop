package br.com.autorepairshop.shared.domain

import br.com.autorepairshop.shared.domain.exception.DomainException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class EmailTest {

    @Test
    fun `trims and lowercases a valid address`() {
        val email = Email.of(raw = "  John.Doe@Email.com  ")

        assertEquals(
            expected = "john.doe@email.com",
            actual = email.value,
        )
    }

    @Test
    fun `rejects short and malformed addresses`() {
        assertFailsWith<DomainException> {
            Email.of(raw = "a@b")
        }
        assertFailsWith<DomainException> {
            Email.of(raw = "john.doe.email.com")
        }
        assertFailsWith<DomainException> {
            Email.of(raw = "${"a".repeat(n = 56)}@x.co")
        }
    }

    @Test
    fun `accepts address of 60 characters`() {
        val sixty = "${"a".repeat(n = 55)}@x.co"

        assertEquals(
            expected = sixty,
            actual = Email.of(raw = sixty).value,
        )
    }

    @Test
    fun `maps a custom exception for invalid input`() {
        class CustomInvalid(message: String) : DomainException(message = message)

        assertFailsWith<CustomInvalid> {
            Email.of(
                raw = "not-an-email",
                invalid = { CustomInvalid(message = it) },
            )
        }
    }
}
