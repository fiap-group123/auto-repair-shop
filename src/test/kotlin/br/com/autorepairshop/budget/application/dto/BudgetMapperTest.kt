package br.com.autorepairshop.budget.application.dto

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.toJavaInstant

@Tag("unit")
class BudgetMapperTest {

    @Test
    fun `maps the budget fields to the response`() {
        val budget = BudgetFixtures.waitingApproval()

        val response = budget.toResponse()

        assertEquals(
            expected = budget.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = budget.serviceOrderId,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = budget.total.amount,
            actual = response.total,
        )
        assertEquals(
            expected = BudgetStatus.WAITING_APPROVAL.name,
            actual = response.status,
        )
        assertEquals(
            expected = budget.createdAt.toJavaInstant(),
            actual = response.createdAt,
        )
        assertNull(response.finishedAt)
    }
}
