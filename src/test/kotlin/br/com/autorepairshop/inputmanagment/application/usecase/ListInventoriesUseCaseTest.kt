package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListInventoriesUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = ListInventoriesUseCase(inventories = inventories)

    @Test
    fun `maps persisted items to responses`() {
        val first = InventoryFixtures.inventory()
        val second = InventoryFixtures.inventory(name = InventoryFixtures.OTHER_NAME)
        every { inventories.findAll() } returns listOf(element = first).plus(element = second)

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = first.id.value,
            actual = response[0].id,
        )
        assertEquals(
            expected = second.id.value,
            actual = response[1].id,
        )
    }

    @Test
    fun `returns empty list when there are no items`() {
        every { inventories.findAll() } returns emptyList()

        val response = useCase.execute(input = Unit)

        assertTrue(response.isEmpty())
    }
}
