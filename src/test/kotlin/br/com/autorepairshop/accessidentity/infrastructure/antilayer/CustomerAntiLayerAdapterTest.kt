package br.com.autorepairshop.accessidentity.infrastructure.antilayer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class CustomerAntiLayerAdapterTest {
    private val customers = mockk<CustomerRepository>()
    private val antiLayer = CustomerAntiLayerAdapter(customers = customers)

    @Test
    fun `maps an active customer`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer

        val record = antiLayer.find(id = customer.id.value)

        assertEquals(
            expected = customer.name.value,
            actual = record?.name,
        )
        assertEquals(
            expected = customer.contact.email.value,
            actual = record?.email,
        )
    }

    @Test
    fun `returns null when missing`() {
        every { customers.findById(id = any()) } returns null

        assertNull(antiLayer.find(id = UUID.randomUUID()))
    }
}
