package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.RegisterExtraServiceCommand
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.event.EventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class RegisterExtraServiceUseCaseTest {
    private val serviceOrders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val extras = mockk<ExtraServiceRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val useCase = RegisterExtraServiceUseCase(
        serviceOrders = serviceOrders,
        services = services,
        extras = extras,
        events = events,
    )

    @Test
    fun `throws when the order is missing`() {
        val serviceOrderId = UUID.randomUUID()
        every { serviceOrders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = command(serviceOrderId = serviceOrderId))
        }
    }

    @Test
    fun `rejects an extra before the budget is approved`() {
        val order = ServiceOrderFixtures.received()
        every { serviceOrders.findById(id = order.id) } returns order

        assertFailsWith<CatalogException.InvalidStatusTransition> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { extras.save(extra = any()) }
    }

    @Test
    fun `rejects a name already used by a service`() {
        val order = ServiceOrderFixtures.budgetApproved()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns true

        assertFailsWith<CatalogException.ServiceAlreadyExists> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { extras.save(extra = any()) }
    }

    @Test
    fun `registers an extra bound to the order and publishes it`() {
        val order = ServiceOrderFixtures.budgetApproved()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { extras.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { extras.save(extra = any()) } returns Unit

        val response = useCase.execute(input = command(serviceOrderId = order.id.value))

        assertEquals(
            expected = CatalogFixtures.OTHER_NAME,
            actual = response.name,
        )
        assertEquals(
            expected = order.id.value,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = "PENDING",
            actual = response.status,
        )
        verify { extras.save(extra = any()) }
        verify { events.publish(aggregate = any()) }
    }

    private fun command(
        serviceOrderId: UUID = UUID.randomUUID(),
        name: String = CatalogFixtures.OTHER_NAME,
        price: String = CatalogFixtures.PRICE,
    ) = RegisterExtraServiceCommand(
        serviceOrderId = serviceOrderId,
        name = name,
        basePrice = BigDecimal(price),
    )
}
