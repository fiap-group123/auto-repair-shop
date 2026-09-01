package br.com.autorepairshop.inputmanagment.domain.aggregate

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryType
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class InventoryTest {

    @Test
    fun `register creates an active catalog item`() {
        val inventory = InventoryFixtures.inventory()

        assertEquals(
            expected = InventoryFixtures.NAME,
            actual = inventory.name.value,
        )
        assertEquals(
            expected = InventoryType.PART,
            actual = inventory.type,
        )
        assertEquals(
            expected = InventoryFixtures.STOCK,
            actual = inventory.stock,
        )
        assertTrue(inventory.active)
    }

    @Test
    fun `rejects negative stock on register`() {
        assertFailsWith<InventoryException.InsufficientStock> {
            InventoryFixtures.inventory(stock = -1)
        }
    }

    @Test
    fun `renames reprices and changes kind while active`() {
        val inventory = InventoryFixtures.inventory()

        inventory.rename(newName = InventoryName.of(raw = InventoryFixtures.OTHER_NAME))
        inventory.reprice(newUnitPrice = InventoryFixtures.money(raw = "50.00"))
        inventory.changeKind(newType = InventoryType.SUPPLY)

        assertEquals(
            expected = InventoryFixtures.OTHER_NAME,
            actual = inventory.name.value,
        )
        assertEquals(
            expected = "50.00",
            actual = inventory.unitPrice.toString(),
        )
        assertEquals(
            expected = InventoryType.SUPPLY,
            actual = inventory.type,
        )
    }

    @Test
    fun `setStock replaces the absolute quantity`() {
        val inventory = InventoryFixtures.inventory()

        inventory.setStock(quantity = 3)

        assertEquals(
            expected = 3,
            actual = inventory.stock,
        )
    }

    @Test
    fun `adjustStock decrements and restores`() {
        val inventory = InventoryFixtures.inventory(stock = 5)

        inventory.adjustStock(delta = -2)
        assertEquals(
            expected = 3,
            actual = inventory.stock,
        )
        inventory.adjustStock(delta = 4)
        assertEquals(
            expected = 7,
            actual = inventory.stock,
        )
    }

    @Test
    fun `adjustStock refuses a negative balance`() {
        val inventory = InventoryFixtures.inventory(stock = 1)

        assertFailsWith<InventoryException.InsufficientStock> {
            inventory.adjustStock(delta = -2)
        }
    }

    @Test
    fun `deactivate blocks mutations and reactivate restores them`() {
        val inventory = InventoryFixtures.inventory()
        inventory.deactivate()
        assertFalse(inventory.active)

        assertFailsWith<InventoryException.InventoryInactive> {
            inventory.rename(newName = InventoryName.of(raw = InventoryFixtures.OTHER_NAME))
        }
        assertFailsWith<InventoryException.InventoryInactive> {
            inventory.adjustStock(delta = -1)
        }

        inventory.adjustStock(delta = 2)
        assertEquals(
            expected = InventoryFixtures.STOCK + 2,
            actual = inventory.stock,
        )

        inventory.reactivate()
        assertTrue(inventory.active)
        assertFailsWith<InventoryException.InventoryAlreadyActive> {
            inventory.reactivate()
        }
    }
}
