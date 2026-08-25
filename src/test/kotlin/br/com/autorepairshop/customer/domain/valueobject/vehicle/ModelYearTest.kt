package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.customer.domain.exception.VehicleException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ModelYearTest {

    @Test
    fun `accepts current year and next year`() {
        val current = ModelYear.of(
            year = 2026,
            currentYear = 2026,
        )
        val next = ModelYear.of(
            year = 2027,
            currentYear = 2026,
        )

        assertEquals(
            expected = 2026,
            actual = current.value,
        )
        assertEquals(
            expected = 2027,
            actual = next.value,
        )
    }

    @Test
    fun `rejects years below 1900 and beyond next year`() {
        assertFailsWith<VehicleException.InvalidModelYear> {
            ModelYear.of(
                year = 1899,
                currentYear = 2026,
            )
        }
        assertFailsWith<VehicleException.InvalidModelYear> {
            ModelYear.of(
                year = 2028,
                currentYear = 2026,
            )
        }
    }
}
