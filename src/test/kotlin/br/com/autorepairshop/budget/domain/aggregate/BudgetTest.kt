package br.com.autorepairshop.budget.domain.aggregate

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.event.BudgetApproved
import br.com.autorepairshop.budget.domain.event.BudgetRejected
import br.com.autorepairshop.budget.domain.event.BudgetTraded
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.shared.domain.Money
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@Tag("unit")
class BudgetTest {

    @Test
    fun `register creates a waiting budget`() {
        val budget = BudgetFixtures.waitingApproval()

        assertEquals(
            expected = BudgetStatus.WAITING_APPROVAL,
            actual = budget.status,
        )
        assertEquals(
            expected = BudgetFixtures.TOTAL,
            actual = budget.total,
        )
        assertNull(budget.finishedAt)
        assertTrue(budget.domainEvents.isEmpty())
    }

    @Test
    fun `updateBudgetTotal replaces the stored amount`() {
        val budget = BudgetFixtures.waitingApproval(total = Money.ZERO)

        budget.updateBudgetTotal(newTotal = BudgetFixtures.TOTAL)

        assertEquals(
            expected = BudgetFixtures.TOTAL,
            actual = budget.total,
        )
    }

    @Test
    fun `approve records the event and finishes the budget`() {
        val budget = BudgetFixtures.waitingApproval()
        val at = Instant.fromEpochSeconds(epochSeconds = 1_700_000_000)

        budget.approve(at = at)

        assertEquals(
            expected = BudgetStatus.APPROVED,
            actual = budget.status,
        )
        assertEquals(
            expected = at,
            actual = budget.finishedAt,
        )
        assertTrue(budget.domainEvents.single() is BudgetApproved)
    }

    @Test
    fun `reject records the event and finishes the budget`() {
        val budget = BudgetFixtures.waitingApproval()

        budget.reject()

        assertEquals(
            expected = BudgetStatus.REJECTED,
            actual = budget.status,
        )
        assertTrue(budget.domainEvents.single() is BudgetRejected)
    }

    @Test
    fun `trade records the event and finishes the budget`() {
        val budget = BudgetFixtures.waitingApproval()

        budget.trade()

        assertEquals(
            expected = BudgetStatus.TRADED,
            actual = budget.status,
        )
        assertTrue(budget.domainEvents.single() is BudgetTraded)
    }

    @Test
    fun `rejects transitions after the budget is no longer waiting`() {
        val budget = BudgetFixtures.waitingApproval()
        budget.approve()

        assertFailsWith<BudgetException.InvalidBudgetStatusTransition> {
            budget.reject()
        }
    }

    @Test
    fun `rehydrate restores state without domain events`() {
        val original = BudgetFixtures.waitingApproval()
        original.updateBudgetTotal(newTotal = Money.of(raw = BigDecimal("80.00")))
        val restored = Budget.rehydrate(
            id = original.id,
            serviceOrderId = original.serviceOrderId,
            total = original.total,
            status = original.status,
            createdAt = original.createdAt,
            finishedAt = original.finishedAt,
        )

        assertEquals(
            expected = original.total,
            actual = restored.total,
        )
        assertTrue(restored.domainEvents.isEmpty())
    }
}
