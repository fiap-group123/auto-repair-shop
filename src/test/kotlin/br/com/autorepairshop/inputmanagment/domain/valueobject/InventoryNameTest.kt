package br.com.autorepairshop.inputmanagment.domain.valueobject

import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class InventoryNameTest {

    @Test
    fun `trims and collapses whitespace`() {
        assertEquals(
            expected = "Filtro de oleo",
            actual = InventoryName.of(raw = "  Filtro   de  oleo ").value,
        )
    }

    @Test
    fun `rejects a name that is too short`() {
        assertFailsWith<InventoryException.InvalidInventoryName> {
            InventoryName.of(raw = "a")
        }
    }

    @Test
    fun `rejects a name that is too long`() {
        assertFailsWith<InventoryException.InvalidInventoryName> {
            InventoryName.of(raw = "a".repeat(n = 61))
        }
    }
}
