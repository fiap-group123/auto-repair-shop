package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.application.dto.RemoveServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class RemoveServiceOrderItemUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = RemoveServiceOrderItemUseCase(orders = orders)

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = id)) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(
                input = RemoveServiceOrderItemCommand(
                    serviceOrderId = id,
                    offeredServiceId = UUID.randomUUID(),
                ),
            )
        }
    }

    @Test
    fun `throws when the item is not in the order`() {
        val order = ServiceOrderFixtures.received()
        every { orders.findById(id = order.id) } returns order

        assertFailsWith<ServiceOrderException.ItemNotFound> {
            useCase.execute(
                input = RemoveServiceOrderItemCommand(
                    serviceOrderId = order.id.value,
                    offeredServiceId = UUID.randomUUID(),
                ),
            )
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `removes the requested service from the budget`() {
        val order = ServiceOrderFixtures.received()
        val offeredServiceId = UUID.randomUUID()
        order.addItem(item = ServiceOrderFixtures.item(offeredServiceId = offeredServiceId))
        every { orders.findById(id = order.id) } returns order
        every { orders.save(order = order) } returns Unit

        val response = useCase.execute(
            input = RemoveServiceOrderItemCommand(
                serviceOrderId = order.id.value,
                offeredServiceId = offeredServiceId,
            ),
        )

        assertTrue(response.items.isEmpty())
        verify { orders.save(order = order) }
    }
}
