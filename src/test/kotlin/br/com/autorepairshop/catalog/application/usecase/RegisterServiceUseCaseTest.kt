package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.exception.DomainException
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
class RegisterServiceUseCaseTest {
    private val serviceOrders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val extras = mockk<ExtraServiceRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val useCase = RegisterServiceUseCase(
        serviceOrders = serviceOrders,
        services = services,
        extras = extras,
        events = events,
    )

    @Test
    fun `rejects a duplicate name in the same order`() {
        val order = ServiceOrderFixtures.received()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns true

        assertFailsWith<CatalogException.ServiceAlreadyExists> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `rejects a name already used by an extra service`() {
        val order = ServiceOrderFixtures.received()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { extras.existsByName(name = any(), serviceOrderId = order.id.value) } returns true

        assertFailsWith<CatalogException.ServiceAlreadyExists> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `rejects a negative price`() {
        val order = ServiceOrderFixtures.received()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { extras.existsByName(name = any(), serviceOrderId = order.id.value) } returns false

        assertFailsWith<DomainException> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    price = "-1.00",
                ),
            )
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `registers a service bound to the order and publishes it`() {
        val order = ServiceOrderFixtures.received()
        every { serviceOrders.findById(id = order.id) } returns order
        every { services.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { extras.existsByName(name = any(), serviceOrderId = order.id.value) } returns false
        every { services.save(service = any()) } returns Unit

        val response = useCase.execute(input = command(serviceOrderId = order.id.value))

        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = response.name,
        )
        assertEquals(
            expected = order.id.value,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = "150.00",
            actual = response.basePrice.toPlainString(),
        )
        verify { services.save(service = any()) }
        verify { events.publish(aggregate = any()) }
    }

    @Test
    fun `rejects a service after the budget is approved`() {
        val order = ServiceOrderFixtures.budgetApproved()
        every { serviceOrders.findById(id = order.id) } returns order

        assertFailsWith<CatalogException.InvalidStatusTransition> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    private fun command(
        serviceOrderId: UUID = UUID.randomUUID(),
        name: String = CatalogFixtures.NAME,
        price: String = CatalogFixtures.PRICE,
    ) = RegisterServiceCommand(
        serviceOrderId = serviceOrderId,
        name = name,
        basePrice = BigDecimal(price),
    )
}
