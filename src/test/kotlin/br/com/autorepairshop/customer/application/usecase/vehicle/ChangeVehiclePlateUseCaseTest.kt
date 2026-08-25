package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
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
class ChangeVehiclePlateUseCaseTest {
    private val vehicles = mockk<VehicleRepository>()
    private val useCase = ChangeVehiclePlateUseCase(vehicles = vehicles)

    @Test
    fun `throws when vehicle is missing`() {
        val vehicleId = UUID.randomUUID()
        every { vehicles.findById(id = VehicleId(value = vehicleId)) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(
                input = ChangeVehiclePlateCommand(
                    vehicleId = vehicleId,
                    plate = CustomerFixtures.NATIONAL_PLATE,
                ),
            )
        }
    }

    @Test
    fun `throws when another vehicle already has the plate`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { vehicles.existsByPlate(plate = any()) } returns true

        assertFailsWith<VehicleException.VehicleAlreadyExists> {
            useCase.execute(
                input = ChangeVehiclePlateCommand(
                    vehicleId = vehicle.id.value,
                    plate = CustomerFixtures.NATIONAL_PLATE,
                ),
            )
        }
    }

    @Test
    fun `same plate is not treated as a duplicate`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { vehicles.save(vehicle = vehicle) } returns Unit

        val response = useCase.execute(
            input = ChangeVehiclePlateCommand(
                vehicleId = vehicle.id.value,
                plate = CustomerFixtures.MERCOSUL_PLATE,
            ),
        )

        assertEquals(
            expected = CustomerFixtures.MERCOSUL_PLATE,
            actual = response.plate,
        )
        verify(exactly = 0) { vehicles.existsByPlate(plate = any()) }
        verify { vehicles.save(vehicle = vehicle) }
    }

    @Test
    fun `changes to a free plate`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { vehicles.existsByPlate(plate = any()) } returns false
        every { vehicles.save(vehicle = vehicle) } returns Unit

        val response = useCase.execute(
            input = ChangeVehiclePlateCommand(
                vehicleId = vehicle.id.value,
                plate = CustomerFixtures.NATIONAL_PLATE,
            ),
        )

        assertEquals(
            expected = "ABC-1234",
            actual = response.plate,
        )
        verify { vehicles.existsByPlate(plate = any()) }
        verify { vehicles.save(vehicle = vehicle) }
    }
}
