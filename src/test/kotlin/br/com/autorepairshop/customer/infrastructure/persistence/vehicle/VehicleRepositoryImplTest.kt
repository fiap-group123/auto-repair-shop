package br.com.autorepairshop.customer.infrastructure.persistence.vehicle

import br.com.autorepairshop.customer.CustomerFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class VehicleRepositoryImplTest {
    private val jpa = mockk<VehicleJpaRepository>()
    private val repo = VehicleRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a vehicle through save and queries`() {
        val vehicle = CustomerFixtures.vehicle()
        val stored = slot<VehicleEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(vehicle = vehicle)

        every { jpa.findById(vehicle.id.value) } returns Optional.of(stored.captured)
        every { jpa.findByPlate(plate = vehicle.plate.value) } returns stored.captured
        every { jpa.existsByPlate(plate = vehicle.plate.value) } returns true
        every { jpa.findAllByOwnerId(ownerId = vehicle.ownerId.value) } returns listOf(element = stored.captured)

        assertEquals(
            expected = vehicle.id,
            actual = repo.findById(id = vehicle.id)?.id,
        )
        assertEquals(
            expected = vehicle.id,
            actual = repo.findByPlate(plate = vehicle.plate)?.id,
        )
        assertTrue(repo.existsByPlate(plate = vehicle.plate))
        assertEquals(
            expected = vehicle.id,
            actual = repo.findByOwner(ownerId = vehicle.ownerId).single().id,
        )
    }

    @Test
    fun `returns null when the vehicle is missing`() {
        val vehicle = CustomerFixtures.vehicle()
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findByPlate(plate = any()) } returns null

        assertNull(repo.findById(id = vehicle.id))
        assertNull(repo.findByPlate(plate = vehicle.plate))
    }
}
