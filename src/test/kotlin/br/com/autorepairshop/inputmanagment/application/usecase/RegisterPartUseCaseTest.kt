package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
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
class RegisterPartUseCaseTest {
    private val serviceOrders = mockk<ServiceOrderRepository>()
    private val inventories = mockk<InventoryRepository>()
    private val parts = mockk<PartRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val useCase = RegisterPartUseCase(
        serviceOrders = serviceOrders,
        inventories = inventories,
        parts = parts,
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
    fun `rejects a part after the budget is approved`() {
        val order = ServiceOrderFixtures.budgetApproved()
        every { serviceOrders.findById(id = order.id) } returns order

        assertFailsWith<InventoryException.InvalidStatusTransition> {
            useCase.execute(input = command(serviceOrderId = order.id.value))
        }
        verify(exactly = 0) { parts.save(part = any()) }
    }

    @Test
    fun `throws when the catalog item is missing`() {
        val order = ServiceOrderFixtures.received()
        val inventoryId = UUID.randomUUID()
        every { serviceOrders.findById(id = order.id) } returns order
        every { inventories.findById(id = any()) } returns null

        assertFailsWith<InventoryException.InventoryNotFound> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    inventoryId = inventoryId,
                ),
            )
        }
    }

    @Test
    fun `rejects a duplicate catalog item on the same order`() {
        val order = ServiceOrderFixtures.received()
        val inventory = InventoryFixtures.inventory()
        every { serviceOrders.findById(id = order.id) } returns order
        every { inventories.findById(id = inventory.id) } returns inventory
        every {
            parts.existsByInventoryId(
                inventoryId = inventory.id,
                serviceOrderId = order.id.value,
            )
        } returns true

        assertFailsWith<InventoryException.PartAlreadyExists> {
            useCase.execute(
                input = command(
                    serviceOrderId = order.id.value,
                    inventoryId = inventory.id.value,
                ),
            )
        }
        verify(exactly = 0) { parts.save(part = any()) }
    }

    @Test
    fun `registers a part decrements stock and publishes`() {
        val order = ServiceOrderFixtures.received()
        val inventory = InventoryFixtures.inventory(stock = 10)
        every { serviceOrders.findById(id = order.id) } returns order
        every { inventories.findById(id = inventory.id) } returns inventory
        every {
            parts.existsByInventoryId(
                inventoryId = inventory.id,
                serviceOrderId = order.id.value,
            )
        } returns false
        every { inventories.save(inventory = inventory) } returns Unit
        every { parts.save(part = any()) } returns Unit

        val response = useCase.execute(
            input = command(
                serviceOrderId = order.id.value,
                inventoryId = inventory.id.value,
                quantity = 3,
            ),
        )

        assertEquals(
            expected = order.id.value,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = inventory.id.value,
            actual = response.inventoryId,
        )
        assertEquals(
            expected = 3,
            actual = response.quantity,
        )
        assertEquals(
            expected = 7,
            actual = inventory.stock,
        )
        verify { inventories.save(inventory = inventory) }
        verify { parts.save(part = any()) }
        verify { events.publish(aggregate = any()) }
    }

    private fun command(
        serviceOrderId: UUID = UUID.randomUUID(),
        inventoryId: UUID = UUID.randomUUID(),
        quantity: Int = 2,
    ) = RegisterPartCommand(
        serviceOrderId = serviceOrderId,
        inventoryId = inventoryId,
        quantity = quantity,
    )
}
