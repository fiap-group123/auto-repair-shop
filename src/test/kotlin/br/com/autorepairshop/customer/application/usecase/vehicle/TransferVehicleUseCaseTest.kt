package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.vehicle.TransferVehicleCommand
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
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
class TransferVehicleUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val vehicles = mockk<VehicleRepository>()
    private val useCase = TransferVehicleUseCase(
        customers = customers,
        vehicles = vehicles,
    )

    @Test
    fun `throws when vehicle is missing`() {
        val vehicleId = UUID.randomUUID()
        every { vehicles.findById(id = VehicleId(value = vehicleId)) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(
                input = TransferVehicleCommand(
                    vehicleId = vehicleId,
                    newOwnerId = UUID.randomUUID(),
                ),
            )
        }
    }

    @Test
    fun `throws when new owner is missing`() {
        val vehicle = CustomerFixtures.vehicle()
        val newOwnerId = UUID.randomUUID()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { customers.findById(id = CustomerId(value = newOwnerId)) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(
                input = TransferVehicleCommand(
                    vehicleId = vehicle.id.value,
                    newOwnerId = newOwnerId,
                ),
            )
        }
    }

    @Test
    fun `throws when new owner is inactive`() {
        val vehicle = CustomerFixtures.vehicle()
        val newOwner = CustomerFixtures.inactiveCustomer()
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { customers.findById(id = newOwner.id) } returns newOwner

        assertFailsWith<CustomerException.InvalidDocument> {
            useCase.execute(
                input = TransferVehicleCommand(
                    vehicleId = vehicle.id.value,
                    newOwnerId = newOwner.id.value,
                ),
            )
        }
    }

    @Test
    fun `throws when transferring to the current owner`() {
        val owner = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = owner)
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { customers.findById(id = owner.id) } returns owner

        assertFailsWith<VehicleException.AlreadyOwnedByCustomer> {
            useCase.execute(
                input = TransferVehicleCommand(
                    vehicleId = vehicle.id.value,
                    newOwnerId = owner.id.value,
                ),
            )
        }
    }

    @Test
    fun `transfers to another active owner`() {
        val owner = CustomerFixtures.activeCustomer()
        val newOwner = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.OTHER_CPF)
        val vehicle = CustomerFixtures.vehicle(owner = owner)
        every { vehicles.findById(id = vehicle.id) } returns vehicle
        every { customers.findById(id = newOwner.id) } returns newOwner
        every { vehicles.save(vehicle = vehicle) } returns Unit

        val response = useCase.execute(
            input = TransferVehicleCommand(
                vehicleId = vehicle.id.value,
                newOwnerId = newOwner.id.value,
            ),
        )

        assertEquals(
            expected = newOwner.id.value,
            actual = response.ownerId,
        )
        verify { vehicles.save(vehicle = vehicle) }
    }
}
