package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ServiceOrderBudgetTest {

    @Test
    fun `total sums every requested service`() {
        val order = ServiceOrderFixtures.received()

        order.addItem(item = ServiceOrderFixtures.item(unitPrice = "150.00"))
        order.addItem(
            item = ServiceOrderFixtures.item(
                unitPrice = "80.50",
                quantity = 2,
            ),
        )

        assertEquals(
            expected = 2,
            actual = order.items.size,
        )
        assertEquals(
            expected = "311.00",
            actual = order.total().toString(),
        )
    }

    @Test
    fun `rejects the same service twice`() {
        val order = ServiceOrderFixtures.received()
        val offeredServiceId = UUID.randomUUID()
        order.addItem(item = ServiceOrderFixtures.item(offeredServiceId = offeredServiceId))

        assertFailsWith<ServiceOrderException.ItemAlreadyAdded> {
            order.addItem(item = ServiceOrderFixtures.item(offeredServiceId = offeredServiceId))
        }
    }

    @Test
    fun `accepts items while in diagnosis`() {
        val order = ServiceOrderFixtures.inDiagnosisWithoutItems()

        order.addItem(item = ServiceOrderFixtures.item())

        assertEquals(
            expected = 1,
            actual = order.items.size,
        )
    }

    @Test
    fun `removes an item`() {
        val order = ServiceOrderFixtures.received()
        val offeredServiceId = UUID.randomUUID()
        order.addItem(item = ServiceOrderFixtures.item(offeredServiceId = offeredServiceId))

        order.removeItem(offeredServiceId = offeredServiceId)

        assertTrue(order.items.isEmpty())
    }

    @Test
    fun `removing an unknown item fails`() {
        val order = ServiceOrderFixtures.received()

        assertFailsWith<ServiceOrderException.ItemNotFound> {
            order.removeItem(offeredServiceId = UUID.randomUUID())
        }
    }

    @Test
    fun `items are locked once the budget is approved`() {
        val order = ServiceOrderFixtures.inExecution()

        assertFailsWith<ServiceOrderException.ItemsLocked> {
            order.addItem(item = ServiceOrderFixtures.item())
        }
        assertFailsWith<ServiceOrderException.ItemsLocked> {
            order.removeItem(offeredServiceId = order.items.first().offeredServiceId)
        }
    }

    @Test
    fun `an empty budget cannot be sent for approval`() {
        val order = ServiceOrderFixtures.inDiagnosisWithoutItems()

        assertFailsWith<ServiceOrderException.EmptyBudget> {
            order.finishDiagnosis()
        }
        assertEquals(
            expected = ServiceOrderStatus.IN_DIAGNOSIS,
            actual = order.status,
        )
    }
}
