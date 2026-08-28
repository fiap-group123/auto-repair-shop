package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceorder.serviceOrderAssembler
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
class FinishDiagnosisUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val events = mockk<EventPublisher>()
    private val useCase = FinishDiagnosisUseCase(
        orders = orders,
        services = services,
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
        every { services.existsByServiceOrderId(serviceOrderId = order.id.value) } returns true

        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `throws when the budget is empty`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { services.existsByServiceOrderId(serviceOrderId = order.id.value) } returns false

        assertFailsWith<ServiceOrderException.EmptyBudget> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `finishes diagnosis and waits for approval`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { services.existsByServiceOrderId(serviceOrderId = order.id.value) } returns true
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
