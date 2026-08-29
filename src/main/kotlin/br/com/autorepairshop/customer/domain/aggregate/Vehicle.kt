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
    color: String,
    active: Boolean,
    val registeredAt: Instant,
) : AggregateRoot<VehicleId>(id = id) {

    var ownerId: CustomerId = ownerId
        private set

    var plate: LicensePlate = plate
        private set

    var brand: String = brand
        private set

    var model: String = model
        private set

    var color: String = color
        private set

    var year: ModelYear = year
        private set

    var active: Boolean = active
        private set

    fun transferTo(newOwnerId: CustomerId) {
        requireActive()
        if (newOwnerId == ownerId) {
            throw VehicleException.AlreadyOwnedByCustomer(
                message = "Vehicle ${plate.formatted()} already belongs to this customer.",
            )
        }
        ownerId = newOwnerId
    }

    fun changePlate(newPlate: LicensePlate) {
        requireActive()
        plate = newPlate
    }

    fun updateSpec(
        brand: String?,
        model: String?,
        color: String?,
        year: ModelYear?,
    ) {
        requireActive()
        brand?.let { this.brand = normalizeName(raw = it, field = "brand") }
        model?.let { this.model = normalizeName(raw = it, field = "model") }
        color?.let { this.color = it }
        year?.let { this.year = it }
    }

    fun reactivate() {
        if (active) throw VehicleException.VehicleAlreadyActive(message = "Vehicle is already active.")
        active = true
    }

    fun deactivate() {
        requireActive()
        active = false
    }

    private fun requireActive() {
        if (!active) {
            throw VehicleException.VehicleInactive(
                message = "Vehicle with plate $plate is inactive.",
            )
        }
    }

    companion object {
        fun register(
            ownerId: CustomerId,
            plate: LicensePlate,
            brand: String,
            model: String,
            color: String,
            year: ModelYear,
            at: Instant = Clock.System.now(),
        ) = Vehicle(
            id = VehicleId.generate(),
            ownerId = ownerId,
            plate = plate,
            brand = normalizeName(raw = brand, field = "brand"),
            model = normalizeName(raw = model, field = "model"),
            color = color,
            year = year,
            active = true,
            registeredAt = at,
        )

        internal fun rehydrate(
            id: VehicleId,
            ownerId: CustomerId,
            plate: LicensePlate,
            brand: String,
            model: String,
            color: String,
            year: ModelYear,
            active: Boolean,
            registeredAt: Instant,
        ) = Vehicle(
            id = id,
            ownerId = ownerId,
            plate = plate,
            brand = brand,
            model = model,
            color = color,
            year = year,
            active = active,
            registeredAt = registeredAt,
        )

        private fun normalizeName(
            raw: String,
            field: String,
        ): String {
            val normalized = raw.trim().replace(
                regex = Regex(pattern = "\\s+"),
                replacement = " ",
            )
            if (normalized.length !in 2..40) {
                throw VehicleException.InvalidVehicleName(
                    message = "$field must be between 2 and 40 characters.",
                )
            }
            return normalized
        }
    }
}
