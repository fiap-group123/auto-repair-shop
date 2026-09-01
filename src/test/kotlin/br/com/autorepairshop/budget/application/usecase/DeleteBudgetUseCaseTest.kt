package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith

@Tag("unit")
class DeleteBudgetUseCaseTest {
    private val budgets = mockk<BudgetRepository>()
    private val useCase = DeleteBudgetUseCase(budgets = budgets)

    @Test
    fun `throws when the budget is missing`() {
        val id = UUID.randomUUID()
        every { budgets.findByServiceOrderId(serviceOrderId = id) } returns null

        assertFailsWith<BudgetException.BudgetNotFound> {
            useCase.execute(input = id)
        }
        verify(exactly = 0) { budgets.deleteByServiceOrderId(serviceOrderId = any()) }
    }

    @Test
    fun `deletes the budget of the order`() {
        val budget = BudgetFixtures.waitingApproval()
        every { budgets.findByServiceOrderId(serviceOrderId = budget.serviceOrderId) } returns budget
        every { budgets.deleteByServiceOrderId(serviceOrderId = budget.serviceOrderId) } returns Unit

        useCase.execute(input = budget.serviceOrderId)

        verify { budgets.deleteByServiceOrderId(serviceOrderId = budget.serviceOrderId) }
    }
}
