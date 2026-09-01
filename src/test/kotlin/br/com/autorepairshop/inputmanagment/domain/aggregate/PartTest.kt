package br.com.autorepairshop.inputmanagment.domain.aggregate

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.event.PartQuantityChanged
import br.com.autorepairshop.inputmanagment.domain.event.PartRegistered
import br.com.autorepairshop.inputmanagment.domain.event.PartRemoved
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class PartTest {

    @Test
    fun `register snapshots the catalog price and emits PartRegistered`() {
        val serviceOrderId = UUID.randomUUID()
        val inventory = InventoryFixtures.inventory(price = "12.50")
        val part = InventoryFixtures.part(
            serviceOrderId = serviceOrderId,
            inventory = inventory,
            quantity = 3,
        )

        assertEquals(
            expected = serviceOrderId,
            actual = part.serviceOrderId,
        )
        assertEquals(
            expected = inventory.id,
            actual = part.inventoryId,
        )
        assertEquals(
            expected = 3,
            actual = part.quantity,
        )
        assertEquals(
            expected = "37.50",
            actual = part.lineTotal().toString(),
        )
        assertTrue(part.domainEvents.any { it is PartRegistered })
    }

    @Test
    fun `rejects a quantity below one`() {
        assertFailsWith<InventoryException.InvalidQuantity> {
            InventoryFixtures.part(quantity = 0)
        }
    }

    @Test
    fun `changeQuantity updates the line and emits PartQuantityChanged`() {
        val part = InventoryFixtures.part(quantity = 2)
        part.clearEvents()

        part.changeQuantity(newQuantity = 5)

        assertEquals(
            expected = 5,
            actual = part.quantity,
        )
        assertTrue(part.domainEvents.any { it is PartQuantityChanged })
    }

    @Test
    fun `remove emits PartRemoved`() {
        val part = InventoryFixtures.part()
        part.clearEvents()

        part.remove()

        assertTrue(part.domainEvents.any { it is PartRemoved })
    }
}
