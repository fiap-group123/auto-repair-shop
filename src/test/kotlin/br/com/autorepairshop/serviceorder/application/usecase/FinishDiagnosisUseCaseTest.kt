package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceorder.serviceOrderAssembler
import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.Money
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FinishDiagnosisUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val budgets = mockk<BudgetRepository>()
    private val events = mockk<EventPublisher>()
    private val useCase = FinishDiagnosisUseCase(
        orders = orders,
        budgets = budgets,
        events = events,
        responses = serviceOrderAssembler(),
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = id)) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the order is not in diagnosis`() {
        val order = ServiceOrderFixtures.received()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)

        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `throws when the budget is missing`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null

        assertFailsWith<BudgetException.EmptyBudget> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `throws when the budget total is zero`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            BudgetFixtures.waitingApproval(serviceOrderId = order.id.value, total = Money.ZERO)

        assertFailsWith<BudgetException.EmptyBudget> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `finishes diagnosis and waits for approval`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.save(order = order) } returns Unit
        every { events.publish(aggregate = order) } returns Unit

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = ServiceOrderStatus.WAITING_APPROVAL.name,
            actual = response.status,
        )
        verify { orders.save(order = order) }
        verify { events.publish(aggregate = order) }
    }
}
