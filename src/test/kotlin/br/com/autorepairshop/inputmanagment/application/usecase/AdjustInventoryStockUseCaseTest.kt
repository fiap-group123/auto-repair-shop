package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.AdjustInventoryStockCommand
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class AdjustInventoryStockUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = AdjustInventoryStockUseCase(inventories = inventories)

    @Test
    fun `throws when the item is missing`() {
        val id = UUID.randomUUID()
        every { inventories.findById(id = InventoryId(value = id)) } returns null

        assertFailsWith<InventoryException.InventoryNotFound> {
            useCase.execute(input = AdjustInventoryStockCommand(inventoryId = id, quantity = 1))
        }
    }

    @Test
    fun `sets the absolute stock quantity`() {
        val inventory = InventoryFixtures.inventory()
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.save(inventory = inventory) } returns Unit

        val response = useCase.execute(
            input = AdjustInventoryStockCommand(
                inventoryId = inventory.id.value,
                quantity = 4,
            ),
        )

        assertEquals(
            expected = 4,
            actual = response.stock,
        )
        verify { inventories.save(inventory = inventory) }
    }
}
