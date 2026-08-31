package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindBudgetUseCaseTest {
    private val budgets = mockk<BudgetRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = FindBudgetUseCase(
        budgets = budgets,
        orders = orders,
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the budget is missing`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null

        assertFailsWith<BudgetException.BudgetNotFound> {
            useCase.execute(input = order.id.value)
        }
    }

    @Test
    fun `returns the budget of the order`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = budget.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = order.id.value,
            actual = response.serviceOrderId,
        )
    }
}
