package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.event.CustomerDeactivated
import br.com.autorepairshop.customer.domain.event.CustomerReactivated
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class CustomerTest {

    @Test
    fun `register creates an active customer`() {
        val customer = CustomerFixtures.activeCustomer()

        assertTrue(customer.active)
        assertEquals(
            expected = CustomerFixtures.NAME,
            actual = customer.name.value,
        )
    }

    @Test
    fun `deactivate records event and blocks mutations`() {
        val customer = CustomerFixtures.activeCustomer()
        customer.deactivate()

        assertFalse(customer.active)
        assertTrue(customer.domainEvents.single() is CustomerDeactivated)
        assertFailsWith<CustomerException.InvalidDocument> {
            customer.rename(newName = PersonName.of(raw = "Jane Doe"))
        }
        assertFailsWith<CustomerException.InvalidDocument> {
            customer.updateContact(
                newContact = ContactInfo.of(
                    email = "jane.doe@email.com",
                    phone = "21987654321",
                ),
            )
        }
    }

    @Test
    fun `second deactivate fails`() {
        val customer = CustomerFixtures.inactiveCustomer()

        assertFailsWith<CustomerException.InvalidDocument> {
            customer.deactivate()
        }
    }

    @Test
    fun `reactivate on active customer fails`() {
        val customer = CustomerFixtures.activeCustomer()

        assertFailsWith<CustomerException.CustomerAlreadyActive> {
            customer.reactivate()
        }
    }

    @Test
    fun `reactivate restores inactive customer`() {
        val customer = CustomerFixtures.inactiveCustomer()
        customer.clearEvents()
        customer.reactivate()

        assertTrue(customer.active)
        assertTrue(customer.domainEvents.single() is CustomerReactivated)
    }

    @Test
    fun `rehydrate restores an inactive customer from persistence`() {
        val original = CustomerFixtures.inactiveCustomer()
        val restored = Customer.rehydrate(
            id = original.id,
            documentId = original.documentId,
            name = original.name,
            contact = original.contact,
            active = false,
            registeredAt = original.registeredAt,
        )

        assertFalse(restored.active)
        assertEquals(
            expected = original.id,
            actual = restored.id,
        )
        restored.reactivate()
        assertTrue(restored.active)
    }
}
