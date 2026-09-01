package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.event.EventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ApproveBudgetUseCaseTest {
    private val budgets = mockk<BudgetRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val events = mockk<EventPublisher>(relaxUnitFun = true)
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ApproveBudgetUseCase(
        budgets = budgets,
        orders = orders,
        events = events,
        access = access,
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
        val order = ServiceOrderFixtures.waitingApproval()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null

        assertFailsWith<BudgetException.BudgetNotFound> {
            useCase.execute(input = order.id.value)
        }
    }

    @Test
    fun `approves the budget and publishes the event`() {
        val order = ServiceOrderFixtures.waitingApproval()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { budgets.save(budget = budget) } returns Unit

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = BudgetStatus.APPROVED.name,
            actual = response.status,
        )
        verify { budgets.save(budget = budget) }
        verify { events.publish(aggregate = budget) }
    }
}
