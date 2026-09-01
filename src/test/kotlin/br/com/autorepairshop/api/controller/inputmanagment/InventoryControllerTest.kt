package br.com.autorepairshop.api.controller.inputmanagment

import br.com.autorepairshop.api.dto.inputmanagment.AdjustInventoryStockRequest
import br.com.autorepairshop.api.dto.inputmanagment.RegisterInventoryRequest
import br.com.autorepairshop.api.dto.inputmanagment.UpdateInventoryRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.application.dto.AdjustInventoryStockCommand
import br.com.autorepairshop.inputmanagment.application.dto.RegisterInventoryCommand
import br.com.autorepairshop.inputmanagment.application.dto.UpdateInventoryCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.application.usecase.AdjustInventoryStockUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.DeactivateInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.FindInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ListInventoriesUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ReactivateInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.UpdateInventoryUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Tag("unit")
class InventoryControllerTest {
    private val registerInventory = mockk<RegisterInventoryUseCase>()
    private val listInventories = mockk<ListInventoriesUseCase>()
    private val findInventory = mockk<FindInventoryUseCase>()
    private val updateInventory = mockk<UpdateInventoryUseCase>()
    private val deactivateInventory = mockk<DeactivateInventoryUseCase>()
    private val reactivateInventory = mockk<ReactivateInventoryUseCase>()
    private val adjustInventoryStock = mockk<AdjustInventoryStockUseCase>()
    private val controller = InventoryController(
        registerInventory = registerInventory,
        listInventories = listInventories,
        findInventory = findInventory,
        updateInventory = updateInventory,
        deactivateInventory = deactivateInventory,
        reactivateInventory = reactivateInventory,
        adjustInventoryStock = adjustInventoryStock,
    )

    @Test
    fun `register maps the request and returns 201`() {
        val inventory = InventoryFixtures.inventory().toResponse()
        every { registerInventory.execute(input = any()) } returns inventory

        withHttpRequest(requestUri = "/inventories") {
            val response = controller.register(
                request = RegisterInventoryRequest(
                    name = InventoryFixtures.NAME,
                    kind = "PART",
                    unitPrice = BigDecimal(InventoryFixtures.PRICE),
                    stock = InventoryFixtures.STOCK,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = inventory.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerInventory.execute(
                input = RegisterInventoryCommand(
                    name = InventoryFixtures.NAME,
                    kind = "PART",
                    unitPrice = BigDecimal(InventoryFixtures.PRICE),
                    stock = InventoryFixtures.STOCK,
                ),
            )
        }
    }

    @Test
    fun `list find update deactivate reactivate and stock delegate to use cases`() {
        val inventory = InventoryFixtures.inventory().toResponse()
        val deactivated = inventory.copy(active = false)
        every { listInventories.execute(input = Unit) } returns listOf(element = inventory)
        every { findInventory.execute(input = inventory.id) } returns inventory
        every { updateInventory.execute(input = any()) } returns inventory
        every { deactivateInventory.execute(input = inventory.id) } returns deactivated
        every { reactivateInventory.execute(input = inventory.id) } returns inventory
        every { adjustInventoryStock.execute(input = any()) } returns inventory.copy(stock = 4)

        assertEquals(
            expected = inventory.id,
            actual = controller.list().body?.single()?.id,
        )
        assertEquals(
            expected = inventory.id,
            actual = controller.findById(id = inventory.id).body?.id,
        )
        assertEquals(
            expected = inventory.id,
            actual = controller.update(
                id = inventory.id,
                request = UpdateInventoryRequest(
                    name = InventoryFixtures.OTHER_NAME,
                    unitPrice = BigDecimal("50.00"),
                    kind = "SUPPLY",
                ),
            ).body?.id,
        )
        assertFalse(controller.deactivate(id = inventory.id).body!!.active)
        assertEquals(
            expected = inventory.id,
            actual = controller.reactivate(id = inventory.id).body?.id,
        )
        assertEquals(
            expected = 4,
            actual = controller.setStock(
                id = inventory.id,
                request = AdjustInventoryStockRequest(quantity = 4),
            ).body?.stock,
        )
        verify {
            updateInventory.execute(
                input = UpdateInventoryCommand(
                    inventoryId = inventory.id,
                    name = InventoryFixtures.OTHER_NAME,
                    unitPrice = BigDecimal("50.00"),
                    kind = "SUPPLY",
                ),
            )
        }
        verify {
            adjustInventoryStock.execute(
                input = AdjustInventoryStockCommand(
                    inventoryId = inventory.id,
                    quantity = 4,
                ),
            )
        }
    }
}
