package br.com.autorepairshop.customer.domain.valueobject.customer

import br.com.autorepairshop.customer.domain.exception.CustomerException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class PersonNameTest {

    @Test
    fun `collapses surrounding and inner spaces`() {
        val name = PersonName.of(raw = "  John   Doe  ")

        assertEquals(
            expected = "John Doe",
            actual = name.value,
        )
    }

    @Test
    fun `rejects names outside 2 to 60 characters`() {
        assertFailsWith<CustomerException.InvalidPersonName> {
            PersonName.of(raw = "A")
        }
        assertFailsWith<CustomerException.InvalidPersonName> {
            PersonName.of(raw = "A".repeat(n = 61))
        }
    }
}
