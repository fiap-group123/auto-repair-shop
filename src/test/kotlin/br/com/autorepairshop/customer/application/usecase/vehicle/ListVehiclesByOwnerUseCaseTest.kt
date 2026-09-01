package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ListVehiclesByOwnerUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val vehicles = mockk<VehicleRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ListVehiclesByOwnerUseCase(
        customers = customers,
        vehicles = vehicles,
        access = access,
    )

    @Test
    fun `throws when owner is missing`() {
        val ownerId = UUID.randomUUID()
        every { customers.findById(id = CustomerId(value = ownerId)) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(input = ownerId)
        }
    }

    @Test
    fun `lists vehicles of an existing owner`() {
        val owner = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = owner)
        every { customers.findById(id = owner.id) } returns owner
        every { vehicles.findByOwner(ownerId = owner.id) } returns listOf(element = vehicle)

        val response = useCase.execute(input = owner.id.value)

        assertEquals(
            expected = 1,
            actual = response.size,
        )
        assertEquals(
            expected = vehicle.id.value,
            actual = response.single().id,
        )
        assertEquals(
            expected = owner.id.value,
            actual = response.single().ownerId,
        )
    }
}
