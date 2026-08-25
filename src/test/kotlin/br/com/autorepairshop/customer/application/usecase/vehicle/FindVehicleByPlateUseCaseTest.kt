package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindVehicleByPlateUseCaseTest {
    private val vehicles = mockk<VehicleRepository>()
    private val useCase = FindVehicleByPlateUseCase(vehicles = vehicles)

    @Test
    fun `throws when plate is not registered`() {
        every { vehicles.findByPlate(plate = any()) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(input = CustomerFixtures.MERCOSUL_PLATE)
        }
    }

    @Test
    fun `returns the vehicle for a registered plate`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findByPlate(plate = vehicle.plate) } returns vehicle

        val response = useCase.execute(input = CustomerFixtures.MERCOSUL_PLATE)

        assertEquals(
            expected = vehicle.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = CustomerFixtures.MERCOSUL_PLATE,
            actual = response.plate,
        )
    }
}
