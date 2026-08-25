package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.VehicleException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class LicensePlateTest {

    @Test
    fun `accepts national plate with hyphen`() {
        val plate = LicensePlate.of(raw = CustomerFixtures.NATIONAL_PLATE)

        assertEquals(
            expected = "ABC1234",
            actual = plate.value,
        )
        assertEquals(
            expected = LicensePlateType.NATIONAl,
            actual = plate.type,
        )
        assertEquals(
            expected = "ABC-1234",
            actual = plate.formatted(),
        )
        assertEquals(
            expected = plate.formatted(),
            actual = plate.toString(),
        )
    }

    @Test
    fun `accepts Mercosul plate without hyphen`() {
        val plate = LicensePlate.of(raw = CustomerFixtures.MERCOSUL_PLATE)

        assertEquals(
            expected = "ABC1D23",
            actual = plate.value,
        )
        assertEquals(
            expected = LicensePlateType.MERCOSUL,
            actual = plate.type,
        )
        assertEquals(
            expected = "ABC1D23",
            actual = plate.formatted(),
        )
    }

    @Test
    fun `rejects invalid plate`() {
        assertFailsWith<VehicleException.InvalidLicensePlate> {
            LicensePlate.of(raw = "XXXX")
        }
    }
}
