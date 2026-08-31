package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class RegisterVehicleUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val vehicles = mockk<VehicleRepository>()
    private val useCase = RegisterVehicleUseCase(
        customers = customers,
        vehicles = vehicles,
    )

    @Test
    fun `throws when owner is missing`() {
        val ownerId = UUID.randomUUID()
        every { customers.findById(id = CustomerId(value = ownerId)) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(input = command(ownerId = ownerId))
        }
    }

    @Test
    fun `throws when owner is inactive`() {
        val owner = CustomerFixtures.inactiveCustomer()
        every { customers.findById(id = owner.id) } returns owner

        assertFailsWith<CustomerException.InvalidDocument> {
            useCase.execute(input = command(ownerId = owner.id.value))
        }
    }

    @Test
    fun `throws when plate already exists`() {
        val owner = CustomerFixtures.activeCustomer()
        every { customers.findById(id = owner.id) } returns owner
        every { vehicles.existsByPlate(plate = any()) } returns true

        assertFailsWith<VehicleException.VehicleAlreadyExists> {
            useCase.execute(input = command(ownerId = owner.id.value))
        }
        verify(exactly = 0) { vehicles.save(vehicle = any()) }
    }

    @Test
    fun `registers a vehicle for an active owner`() {
        val owner = CustomerFixtures.activeCustomer()
        every { customers.findById(id = owner.id) } returns owner
        every { vehicles.existsByPlate(plate = any()) } returns false
        every { vehicles.save(vehicle = any()) } returns Unit

        val response = useCase.execute(input = command(ownerId = owner.id.value))

        assertEquals(
            expected = CustomerFixtures.MERCOSUL_PLATE,
            actual = response.plate,
        )
        assertEquals(
            expected = owner.id.value,
            actual = response.ownerId,
        )
        verify { vehicles.save(vehicle = any()) }
    }

    private fun command(ownerId: UUID) = RegisterVehicleCommand(
        ownerId = ownerId,
        plate = CustomerFixtures.MERCOSUL_PLATE,
        brand = "Fiat",
        model = "Argo",
        color = "Preto",
        year = 2024,
    )
}
