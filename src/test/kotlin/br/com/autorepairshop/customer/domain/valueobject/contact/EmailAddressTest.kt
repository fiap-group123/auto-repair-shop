package br.com.autorepairshop.customer.domain.valueobject.contact

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class EmailAddressTest {

    @Test
    fun `trims and lowercases a valid address`() {
        val email = EmailAddress.of(raw = "  John.Doe@Email.com  ")

        assertEquals(
            expected = CustomerFixtures.EMAIL,
            actual = email.value,
        )
    }

    @Test
    fun `rejects short and malformed addresses`() {
        assertFailsWith<CustomerException.InvalidEmailAddress> {
            EmailAddress.of(raw = "a@b")
        }
        assertFailsWith<CustomerException.InvalidEmailAddress> {
            EmailAddress.of(raw = "john.doe.email.com")
        }
        assertFailsWith<CustomerException.InvalidEmailAddress> {
            EmailAddress.of(raw = "${"a".repeat(n = 56)}@x.co")
        }
    }
}
