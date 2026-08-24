package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.domain.AggregateRoot
import kotlin.time.Clock
import kotlin.time.Instant

class Vehicle private constructor(
    id: VehicleId,
    ownerId: CustomerId,
    plate: LicensePlate,
    brand: String,
    model: String,
    year: ModelYear,
    val registeredAt: Instant,
) : AggregateRoot<VehicleId>(id) {

    var ownerId: CustomerId = ownerId
        private set

    var plate: LicensePlate = plate
        private set

    var brand: String = brand
        private set

    var model: String = model
        private set

    var year: ModelYear = year
        private set

    fun transferTo(newOwnerId: CustomerId) {
        if (newOwnerId == ownerId) {
            throw VehicleException.AlreadyOwnedByCustomer(
                "Vehicle ${plate.formatted()} already belongs to this customer."
            )
        }
        ownerId = newOwnerId
    }

    fun changePlate(newPlate: LicensePlate) {
        plate = newPlate
    }

    fun updateSpec(brand: String, model: String, year: ModelYear) {
        this.brand = normalizeName(brand, field = "brand")
        this.model = normalizeName(model, field = "model")
        this.year = year
    }

    companion object {
        fun register(
            ownerId: CustomerId,
            plate: LicensePlate,
            brand: String,
            model: String,
            year: ModelYear,
            at: Instant = Clock.System.now(),
        ) = Vehicle(
            id = VehicleId.generate(),
            ownerId = ownerId,
            plate = plate,
            brand = normalizeName(brand, field = "brand"),
            model = normalizeName(model, field = "model"),
            year = year,
            registeredAt = at,
        )

        internal fun rehydrate(
            id: VehicleId,
            ownerId: CustomerId,
            plate: LicensePlate,
            brand: String,
            model: String,
            year: ModelYear,
            registeredAt: Instant,
        ) = Vehicle(
            id = id,
            ownerId = ownerId,
            plate = plate,
            brand = brand,
            model = model,
            year = year,
            registeredAt = registeredAt,
        )

        private fun normalizeName(raw: String, field: String): String {
            val normalized = raw.trim().replace(Regex("\\s+"), " ")
            if (normalized.length !in 2..40) {
                throw VehicleException.InvalidVehicleName(
                    "$field must be between 2 and 40 characters."
                )
            }
            return normalized
        }
    }
}