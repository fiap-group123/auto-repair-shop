package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class VehicleTest {

    @Test
    fun `transfer to the same owner fails`() {
        val owner = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = owner)

        assertFailsWith<VehicleException.AlreadyOwnedByCustomer> {
            vehicle.transferTo(newOwnerId = owner.id)
        }
    }

    @Test
    fun `transfer updates the owner`() {
        val owner = CustomerFixtures.activeCustomer()
        val newOwner = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.OTHER_CPF)
        val vehicle = CustomerFixtures.vehicle(owner = owner)

        vehicle.transferTo(newOwnerId = newOwner.id)

        assertEquals(
            expected = newOwner.id,
            actual = vehicle.ownerId,
        )
    }

    @Test
    fun `updateSpec collapses spaces and rejects short names`() {
        val vehicle = CustomerFixtures.vehicle()
        vehicle.updateSpec(
            brand = "  Fiat  Power  ",
            model = null,
            color = null,
            year = null,
        )

        assertEquals(
            expected = "Fiat Power",
            actual = vehicle.brand,
        )
        assertFailsWith<VehicleException.InvalidVehicleName> {
            vehicle.updateSpec(
                brand = "A",
                model = null,
                color = null,
                year = null,
            )
        }
        assertFailsWith<VehicleException.InvalidVehicleName> {
            vehicle.updateSpec(
                brand = null,
                model = "M".repeat(n = 41),
                color = null,
                year = null,
            )
        }
    }

    @Test
    fun `changePlate replaces the plate`() {
        val vehicle = CustomerFixtures.vehicle()
        val national = LicensePlate.of(raw = CustomerFixtures.NATIONAL_PLATE)

        vehicle.changePlate(newPlate = national)

        assertEquals(
            expected = national,
            actual = vehicle.plate,
        )
    }

    @Test
    fun `updateSpec applies a new year`() {
        val vehicle = CustomerFixtures.vehicle()
        val year = ModelYear.of(
            year = 2020,
            currentYear = 2026,
        )

        vehicle.updateSpec(
            brand = null,
            model = "Pulse",
            color = null,
            year = year,
        )

        assertEquals(
            expected = "Pulse",
            actual = vehicle.model,
        )
        assertEquals(
            expected = year,
            actual = vehicle.year,
        )
    }

    @Test
    fun `rehydrate restores a vehicle from persistence`() {
        val original = CustomerFixtures.vehicle()
        val restored = Vehicle.rehydrate(
            id = original.id,
            ownerId = original.ownerId,
            plate = original.plate,
            brand = original.brand,
            model = original.model,
            color = original.color,
            year = original.year,
            active = original.active,
            registeredAt = original.registeredAt,
        )

        assertEquals(
            expected = original.id,
            actual = restored.id,
        )
        assertEquals(
            expected = original.plate,
            actual = restored.plate,
        )
        assertEquals(
            expected = original.ownerId,
            actual = restored.ownerId,
        )
    }
}
