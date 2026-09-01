package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindVehicleUseCaseTest {
    private val vehicles = mockk<VehicleRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = FindVehicleUseCase(
        vehicles = vehicles,
        access = access,
    )

    @Test
    fun `throws when vehicle is missing`() {
        val id = UUID.randomUUID()
        every { vehicles.findById(id = VehicleId(value = id)) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `returns the vehicle when found`() {
        val vehicle = CustomerFixtures.vehicle()
        every { vehicles.findById(id = vehicle.id) } returns vehicle

        val response = useCase.execute(input = vehicle.id.value)

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
