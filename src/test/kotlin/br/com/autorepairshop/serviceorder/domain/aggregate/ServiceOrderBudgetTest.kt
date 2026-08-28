package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ServiceOrderBudgetTest {

    @Test
    fun `services can change while the order is received or in diagnosis`() {
        ServiceOrderFixtures.received().requireItemsEditable()
        ServiceOrderFixtures.inDiagnosis().requireItemsEditable()
    }

    @Test
    fun `services are locked once the budget is approved`() {
        val order = ServiceOrderFixtures.inExecution()

        assertFailsWith<ServiceOrderException.ItemsLocked> {
            order.requireItemsEditable()
        }
    }

    @Test
    fun `an empty budget cannot be sent for approval`() {
        val order = ServiceOrderFixtures.inDiagnosis()

        assertFailsWith<ServiceOrderException.EmptyBudget> {
            order.finishDiagnosis(hasServices = false)
        }
        assertEquals(
            expected = ServiceOrderStatus.IN_DIAGNOSIS,
            actual = order.status,
        )
    }
}
