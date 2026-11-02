package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.application.dto.ReceiveBudgetDecisionCommand
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
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
class ReceiveBudgetDecisionUseCaseTest {
    private val budgets = mockk<BudgetRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val events = mockk<EventPublisher>(relaxUnitFun = true)
    private val useCase = ReceiveBudgetDecisionUseCase(
        budgets = budgets,
        orders = orders,
        events = events,
    )

    @Test
    fun `approves the budget from an external decision`() {
        val order = ServiceOrderFixtures.waitingApproval()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { budgets.save(budget = budget) } returns Unit

        val response = useCase.execute(
            input = ReceiveBudgetDecisionCommand(
                serviceOrderId = order.id.value,
                decision = " approved ",
            ),
        )

        assertEquals(expected = BudgetStatus.APPROVED.name, actual = response.status)
        verify { events.publish(aggregate = budget) }
    }

    @Test
    fun `rejects the budget from an external decision`() {
        val order = ServiceOrderFixtures.waitingApproval()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { budgets.save(budget = budget) } returns Unit

        val response = useCase.execute(
            input = ReceiveBudgetDecisionCommand(
                serviceOrderId = order.id.value,
                decision = "rejected",
            ),
        )

        assertEquals(expected = BudgetStatus.REJECTED.name, actual = response.status)
    }

    @Test
    fun `rejects an unknown decision`() {
        val order = ServiceOrderFixtures.waitingApproval()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget

        assertFailsWith<BudgetException.InvalidBudgetStatusTransition> {
            useCase.execute(
                input = ReceiveBudgetDecisionCommand(
                    serviceOrderId = order.id.value,
                    decision = "MAYBE",
                ),
            )
        }
    }

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(
                input = ReceiveBudgetDecisionCommand(
                    serviceOrderId = id,
                    decision = "APPROVED",
                ),
            )
        }
    }

    @Test
    fun `throws when the budget is missing`() {
        val order = ServiceOrderFixtures.waitingApproval()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null

        assertFailsWith<BudgetException.BudgetNotFound> {
            useCase.execute(
                input = ReceiveBudgetDecisionCommand(
                    serviceOrderId = order.id.value,
                    decision = "APPROVED",
                ),
            )
        }
    }
}
