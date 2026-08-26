package br.com.autorepairshop.serviceorder.domain.valueobject

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ServiceOrderItemTest {

    @Test
    fun `subtotal multiplies the unit price by the quantity`() {
        val item = ServiceOrderFixtures.item(
            unitPrice = "99.90",
            quantity = 3,
        )

        assertEquals(
            expected = "299.70",
            actual = item.subtotal().toString(),
        )
    }

    @Test
    fun `rejects a quantity below one`() {
        assertFailsWith<ServiceOrderException.InvalidQuantity> {
            ServiceOrderFixtures.item(quantity = 0)
        }
    }

    @Test
    fun `rejects a quantity above the maximum`() {
        assertFailsWith<ServiceOrderException.InvalidQuantity> {
            ServiceOrderFixtures.item(quantity = 100)
        }
    }
}
