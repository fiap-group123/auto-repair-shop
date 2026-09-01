package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceandexecution.serviceOrderAssembler
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
class StartExecutionUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val events = mockk<EventPublisher>()
    private val useCase = StartExecutionUseCase(
        orders = orders,
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
    fun `throws when the order is not budget approved`() {
        val order = ServiceOrderFixtures.waitingApproval()
        every { orders.findById(id = order.id) } returns order

        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `starts execution on a budget approved order`() {
        val order = ServiceOrderFixtures.budgetApproved()
        every { orders.findById(id = order.id) } returns order
        every { orders.save(order = order) } returns Unit
        every { events.publish(aggregate = order) } returns Unit

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = ServiceOrderStatus.IN_EXECUTION.name,
            actual = response.status,
        )
        verify { orders.save(order = order) }
        verify { events.publish(aggregate = order) }
    }
}
