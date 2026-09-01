package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
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
class DeletePartUseCaseTest {
    private val serviceOrders = mockk<ServiceOrderRepository>()
    private val inventories = mockk<InventoryRepository>()
    private val parts = mockk<PartRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val useCase = DeletePartUseCase(
        serviceOrders = serviceOrders,
        inventories = inventories,
        parts = parts,
        events = events,
    )

    @Test
    fun `throws when the part is missing`() {
        val id = UUID.randomUUID()
        every { parts.findById(id = PartId(value = id)) } returns null

        assertFailsWith<InventoryException.PartNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the order is missing`() {
        val part = InventoryFixtures.part()
        every { parts.findById(id = part.id) } returns part
        every { serviceOrders.findById(id = any()) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = part.id.value)
        }
    }

    @Test
    fun `rejects removal after the budget is approved`() {
        val order = ServiceOrderFixtures.budgetApproved()
        val part = InventoryFixtures.part(serviceOrderId = order.id.value)
        every { parts.findById(id = part.id) } returns part
        every { serviceOrders.findById(id = order.id) } returns order

        assertFailsWith<InventoryException.InvalidStatusTransition> {
            useCase.execute(input = part.id.value)
        }
        verify(exactly = 0) { parts.delete(part = any()) }
    }

    @Test
    fun `restores stock and deletes the part`() {
        val order = ServiceOrderFixtures.received()
        val inventory = InventoryFixtures.inventory(stock = 5)
        val part = InventoryFixtures.part(
            serviceOrderId = order.id.value,
            inventory = inventory,
            quantity = 3,
        )
        every { parts.findById(id = part.id) } returns part
        every { serviceOrders.findById(id = order.id) } returns order
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.save(inventory = inventory) } returns Unit
        every { parts.delete(part = part) } returns Unit

        useCase.execute(input = part.id.value)

        assertEquals(
            expected = 8,
            actual = inventory.stock,
        )
        verify { events.publish(aggregate = part) }
        verify { parts.delete(part = part) }
    }
}
