package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindInventoryUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = FindInventoryUseCase(inventories = inventories)

    @Test
    fun `throws when the item is missing`() {
        val id = UUID.randomUUID()
        every { inventories.findById(id = InventoryId(value = id)) } returns null

        assertFailsWith<InventoryException.InventoryNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `returns the item when found`() {
        val inventory = InventoryFixtures.inventory()
        every { inventories.findById(id = inventory.id) } returns inventory

        val response = useCase.execute(input = inventory.id.value)

        assertEquals(
            expected = inventory.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = InventoryFixtures.NAME,
            actual = response.name,
        )
    }
}
