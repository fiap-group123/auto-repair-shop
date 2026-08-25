package br.com.autorepairshop.customer.domain.valueobject.contact

import br.com.autorepairshop.customer.domain.exception.CustomerException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class PhoneNumberTest {

    @Test
    fun `accepts 10 and 11 digits and strips non-digits`() {
        val landline = PhoneNumber.of(raw = "(11) 3456-7890")
        val mobile = PhoneNumber.of(raw = "(11) 98765-4321")

        assertEquals(
            expected = "1134567890",
            actual = landline.value,
        )
        assertEquals(
            expected = "11987654321",
            actual = mobile.value,
        )
    }

    @Test
    fun `rejects fewer than 10 digits`() {
        assertFailsWith<CustomerException.InvalidPhoneNumber> {
            PhoneNumber.of(raw = "119876543")
        }
        assertFailsWith<CustomerException.InvalidPhoneNumber> {
            PhoneNumber.of(raw = "119876543210")
        }
    }
}
