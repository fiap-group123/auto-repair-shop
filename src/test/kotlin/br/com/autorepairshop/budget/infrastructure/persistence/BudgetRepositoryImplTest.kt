package br.com.autorepairshop.budget.infrastructure.persistence

import br.com.autorepairshop.budget.BudgetFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class BudgetRepositoryImplTest {
    private val jpa = mockk<BudgetJpaRepository>()
    private val repo = BudgetRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a budget through save and find`() {
        val budget = BudgetFixtures.waitingApproval()
        val stored = slot<BudgetEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(budget = budget)

        every { jpa.findByServiceOrderId(serviceOrderId = budget.serviceOrderId) } returns stored.captured

        val loaded = repo.findByServiceOrderId(serviceOrderId = budget.serviceOrderId)
        assertEquals(
            expected = budget.id,
            actual = loaded?.id,
        )
        assertEquals(
            expected = budget.total.amount,
            actual = stored.captured.total,
        )
        assertEquals(
            expected = budget.status.name,
            actual = stored.captured.status.name,
        )
    }

    @Test
    fun `returns null when nothing is stored`() {
        every { jpa.findByServiceOrderId(serviceOrderId = any()) } returns null

        assertNull(repo.findByServiceOrderId(serviceOrderId = UUID.randomUUID()))
    }

    @Test
    fun `deletes by service order id`() {
        val serviceOrderId = UUID.randomUUID()
        every { jpa.deleteByServiceOrderId(serviceOrderId = serviceOrderId) } returns Unit

        repo.deleteByServiceOrderId(serviceOrderId = serviceOrderId)

        verify { jpa.deleteByServiceOrderId(serviceOrderId = serviceOrderId) }
    }
}
