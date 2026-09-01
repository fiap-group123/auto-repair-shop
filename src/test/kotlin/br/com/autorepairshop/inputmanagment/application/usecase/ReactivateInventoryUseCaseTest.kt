package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ReactivateInventoryUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = ReactivateInventoryUseCase(inventories = inventories)

    @Test
    fun `throws when the item is missing`() {
        val id = UUID.randomUUID()
        every { inventories.findById(id = InventoryId(value = id)) } returns null

        assertFailsWith<InventoryException.InventoryNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `reactivates an inactive item`() {
        val inventory = InventoryFixtures.inventory()
        inventory.deactivate()
        every { inventories.findById(id = inventory.id) } returns inventory
        every { inventories.save(inventory = inventory) } returns Unit

        val response = useCase.execute(input = inventory.id.value)

        assertTrue(response.active)
        verify { inventories.save(inventory = inventory) }
    }
}
