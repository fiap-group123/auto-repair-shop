package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.domain.Money
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ServiceOrderBudgetTest {

    @Test
    fun `opens with an empty total`() {
        val order = ServiceOrderFixtures.received()

        assertEquals(
            expected = Money.ZERO,
            actual = order.total,
        )
    }

    @Test
    fun `the budget accepts a total while received or in diagnosis`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        order.updateBudgetTotal(total = ServiceOrderFixtures.TOTAL)

        assertEquals(
            expected = ServiceOrderFixtures.TOTAL,
            actual = order.total,
        )
    }

    @Test
    fun `the total is frozen once the budget is sent for approval`() {
        val order = ServiceOrderFixtures.waitingApproval()

        assertEquals(
            expected = ServiceOrderFixtures.TOTAL,
            actual = order.total,
        )
    }

    @Test
    fun `an empty budget cannot be sent for approval`() {
        val order = ServiceOrderFixtures.inDiagnosis()

        assertFailsWith<ServiceOrderException.EmptyBudget> {
            order.finishDiagnosis()
        }
        assertEquals(
            expected = ServiceOrderStatus.IN_DIAGNOSIS,
            actual = order.status,
        )
    }
}
