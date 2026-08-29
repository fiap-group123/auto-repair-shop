package br.com.autorepairshop.catalog.domain.valueobject

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ServiceNameTest {

    @Test
    fun `trims and collapses whitespace`() {
        assertEquals(
            expected = "Troca de oleo",
            actual = ServiceName.of(raw = "  Troca   de  oleo ").value,
        )
    }

    @Test
    fun `rejects a name that is too short`() {
        assertFailsWith<CatalogException.InvalidServiceName> {
            ServiceName.of(raw = "a")
        }
    }

    @Test
    fun `rejects a name that is too long`() {
        assertFailsWith<CatalogException.InvalidServiceName> {
            ServiceName.of(raw = "a".repeat(n = 61))
        }
    }
}
