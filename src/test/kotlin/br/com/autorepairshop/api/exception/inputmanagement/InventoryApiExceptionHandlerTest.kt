package br.com.autorepairshop.api.exception.inputmanagement

import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class InventoryApiExceptionHandlerTest {
    private val handler = InventoryApiExceptionHandler()

    @Test
    fun `maps domain exceptions to http statuses`() {
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InventoryNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.NOT_FOUND.value(),
            actual = handler.handleInventory(
                ex = InventoryException.PartNotFound(message = "missing"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InventoryAlreadyExists(message = "exists"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.CONFLICT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.PartAlreadyExists(message = "exists"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InsufficientStock(message = "stock"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InventoryInactive(message = "inactive"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InventoryAlreadyActive(message = "active"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InvalidInventoryName(message = "name"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InvalidQuantity(message = "qty"),
            ).status,
        )
        assertEquals(
            expected = HttpStatus.UNPROCESSABLE_CONTENT.value(),
            actual = handler.handleInventory(
                ex = InventoryException.InvalidStatusTransition(message = "invalid"),
            ).status,
        )
    }
}
