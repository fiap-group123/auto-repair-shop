package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.application.dto.AddServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class AddServiceOrderItemUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<OfferedServiceRepository>()
    private val useCase = AddServiceOrderItemUseCase(
        orders = orders,
        services = services,
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = id)) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = command(serviceOrderId = id))
        }
    }

    @Test
    fun `throws when the offered service is missing`() {
        val order = ServiceOrderFixtures.received()
        val offeredServiceId = UUID.randomUUID()
        every { orders.findById(id = order.id) } returns order
        every { services.findById(id = OfferedServiceId(value = offeredServiceId)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    offeredServiceId = offeredServiceId,
                ),
            )
        }
    }

    @Test
    fun `throws when the offered service is inactive`() {
        val order = ServiceOrderFixtures.received()
        val service = CatalogFixtures.inactiveService()
        every { orders.findById(id = order.id) } returns order
        every { services.findById(id = service.id) } returns service

        assertFailsWith<CatalogException.ServiceInactive> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    offeredServiceId = service.id.value,
                ),
            )
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `throws when the items are locked`() {
        val order = ServiceOrderFixtures.inExecution()
        val service = CatalogFixtures.activeService()
        every { orders.findById(id = order.id) } returns order
        every { services.findById(id = service.id) } returns service

        assertFailsWith<ServiceOrderException.ItemsLocked> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    offeredServiceId = service.id.value,
                ),
            )
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `adds the requested service to the budget`() {
        val order = ServiceOrderFixtures.received()
        val service = CatalogFixtures.activeService()
        every { orders.findById(id = order.id) } returns order
        every { services.findById(id = service.id) } returns service
        every { orders.save(order = order) } returns Unit

        val response = useCase.execute(
            input = command(
                serviceOrderId = order.id.value,
                offeredServiceId = service.id.value,
                quantity = 2,
            ),
        )

        assertEquals(
            expected = 1,
            actual = response.items.size,
        )
        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = response.items[0].description,
        )
        assertEquals(
            expected = "300.00",
            actual = response.total.toPlainString(),
        )
        verify { orders.save(order = order) }
    }

    private fun command(
        serviceOrderId: UUID = UUID.randomUUID(),
        offeredServiceId: UUID = UUID.randomUUID(),
        quantity: Int = 1,
    ) = AddServiceOrderItemCommand(
        serviceOrderId = serviceOrderId,
        offeredServiceId = offeredServiceId,
        quantity = quantity,
    )
}
