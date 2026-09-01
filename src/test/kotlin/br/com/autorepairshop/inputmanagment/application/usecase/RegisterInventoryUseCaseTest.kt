package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.RegisterInventoryCommand
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class RegisterInventoryUseCaseTest {
    private val inventories = mockk<InventoryRepository>()
    private val useCase = RegisterInventoryUseCase(inventories = inventories)

    @Test
    fun `rejects a duplicate name`() {
        every { inventories.existsByName(name = any()) } returns true

        assertFailsWith<InventoryException.InventoryAlreadyExists> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { inventories.save(inventory = any()) }
    }

    @Test
    fun `rejects an unknown kind`() {
        every { inventories.existsByName(name = any()) } returns false

        assertFailsWith<InventoryException.InvalidInventoryName> {
            useCase.execute(input = command(kind = "TOOL"))
        }
    }

    @Test
    fun `registers an inventory item`() {
        every { inventories.existsByName(name = any()) } returns false
        every { inventories.save(inventory = any()) } returns Unit

        val response = useCase.execute(input = command())

        assertEquals(
            expected = InventoryFixtures.NAME,
            actual = response.name,
        )
        assertEquals(
            expected = "PART",
            actual = response.kind,
        )
        assertEquals(
            expected = InventoryFixtures.STOCK,
            actual = response.stock,
        )
        verify { inventories.save(inventory = any()) }
    }

    private fun command(
        name: String = InventoryFixtures.NAME,
        kind: String = "PART",
        price: String = InventoryFixtures.PRICE,
        stock: Int = InventoryFixtures.STOCK,
    ) = RegisterInventoryCommand(
        name = name,
        kind = kind,
        unitPrice = BigDecimal(price),
        stock = stock,
    )
}
