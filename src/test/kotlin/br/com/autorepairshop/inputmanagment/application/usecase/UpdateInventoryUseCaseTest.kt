package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.UpdateInventoryCommand
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
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
class UpdateInventoryUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = UpdateInventoryUseCase(inventories = inventories)

    @Test
    fun `throws when the item is missing`() {
        val id = UUID.randomUUID()
        every { inventories.findById(id = InventoryId(value = id)) } returns null

        assertFailsWith<InventoryException.InventoryNotFound> {
            useCase.execute(input = UpdateInventoryCommand(inventoryId = id, name = null, unitPrice = null, kind = null))
        }
    }

    @Test
    fun `rejects renaming to an existing name`() {
        val inventory = InventoryFixtures.inventory()
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.existsByName(name = any()) } returns true

        assertFailsWith<InventoryException.InventoryAlreadyExists> {
            useCase.execute(
                input = UpdateInventoryCommand(
                    inventoryId = inventory.id.value,
                    name = InventoryFixtures.OTHER_NAME,
                    unitPrice = null,
                    kind = null,
                ),
            )
        }
        verify(exactly = 0) { inventories.save(inventory = any()) }
    }

    @Test
    fun `keeping the same name skips the uniqueness check`() {
        val inventory = InventoryFixtures.inventory()
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.save(inventory = inventory) } returns Unit

        val response = useCase.execute(
            input = UpdateInventoryCommand(
                inventoryId = inventory.id.value,
                name = InventoryFixtures.NAME,
                unitPrice = null,
                kind = null,
            ),
        )

        assertEquals(
            expected = InventoryFixtures.NAME,
            actual = response.name,
        )
        verify(exactly = 0) { inventories.existsByName(name = any()) }
        verify { inventories.save(inventory = inventory) }
    }

    @Test
    fun `renames reprices and changes kind`() {
        val inventory = InventoryFixtures.inventory()
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.existsByName(name = any()) } returns false
        every { inventories.save(inventory = inventory) } returns Unit

        val response = useCase.execute(
            input = UpdateInventoryCommand(
                inventoryId = inventory.id.value,
                name = InventoryFixtures.OTHER_NAME,
                unitPrice = BigDecimal("50.00"),
                kind = "SUPPLY",
            ),
        )

        assertEquals(
            expected = InventoryFixtures.OTHER_NAME,
            actual = response.name,
        )
        assertEquals(
            expected = "50.00",
            actual = response.unitPrice.toPlainString(),
        )
        assertEquals(
            expected = "SUPPLY",
            actual = response.kind,
        )
        verify { inventories.save(inventory = inventory) }
    }
}
