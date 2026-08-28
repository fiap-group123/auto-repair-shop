package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.application.dto.vehicle.UpdateVehicleSpecCommand
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class UpdateVehicleSpecUseCaseTest {
    private val vehicles = mockk<VehicleRepository>()
    private val useCase = UpdateVehicleSpecUseCase(vehicles = vehicles)

    @Test
    fun `throws when vehicle is missing`() {
        val vehicleId = UUID.randomUUID()
        every { vehicles.findById(id = VehicleId(value = vehicleId)) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(
                input = UpdateVehicleSpecCommand(
                    vehicleId = vehicleId,
                    brand = "Honda",
                ),
            )
        }
    }

    @Test
    fun `throws when brand is too short`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle

        assertFailsWith<VehicleException.InvalidVehicleName> {
            useCase.execute(
                input = UpdateVehicleSpecCommand(
                    vehicleId = vehicle.id.value,
                    brand = "A",
                ),
            )
        }
    }

    @Test
    fun `throws when vehicle is inactive`() {
        val vehicle = CustomerFixtures.inactiveVehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle

        assertFailsWith<VehicleException.VehicleInactive> {
            useCase.execute(input = UpdateVehicleSpecCommand(
                vehicleId = vehicle.id.value,
                brand = "Honda",
                model = "Civic",
                color = "Preto",
                year = 2026
            ))
        }
    }

    @Test
    fun `updates brand model and year`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { vehicles.save(vehicle = vehicle) } returns Unit

        val response = useCase.execute(
            input = UpdateVehicleSpecCommand(
                vehicleId = vehicle.id.value,
                brand = "Honda",
                model = "Civic",
                color = "Preto",
                year = 2020,
            ),
        )

        assertEquals(
            expected = "Honda",
            actual = response.brand,
        )
        assertEquals(
            expected = "Civic",
            actual = response.model,
        )
        assertEquals(
            expected = 2020,
            actual = response.year,
        )
        verify { vehicles.save(vehicle = vehicle) }
    }
}
