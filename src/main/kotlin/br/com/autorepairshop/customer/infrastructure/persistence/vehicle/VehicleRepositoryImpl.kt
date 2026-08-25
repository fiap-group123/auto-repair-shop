package br.com.autorepairshop.customer.infrastructure.persistence.vehicle

import br.com.autorepairshop.customer.domain.aggregate.Vehicle
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import org.springframework.stereotype.Repository
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class VehicleRepositoryImpl(private val jpa: VehicleJpaRepository) : VehicleRepository {

    override fun save(vehicle: Vehicle) {
        jpa.save(vehicle.toEntity())
    }

    override fun findById(id: VehicleId): Vehicle? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByPlate(plate: LicensePlate): Vehicle? = jpa.findByPlate(plate = plate.value)?.toDomain()

    override fun findByOwner(ownerId: CustomerId): List<Vehicle> =
        jpa.findAllByOwnerId(ownerId = ownerId.value).map { it.toDomain() }

    override fun existsByPlate(plate: LicensePlate): Boolean = jpa.existsByPlate(plate = plate.value)

    private fun Vehicle.toEntity() = VehicleEntity(
        id = id.value,
        ownerId = ownerId.value,
        plate = plate.value,
        brand = brand,
        model = model,
        year = year.value,
        registeredAt = registeredAt.toJavaInstant(),
    )

    private fun VehicleEntity.toDomain() = Vehicle.rehydrate(
        id = VehicleId(value = id),
        ownerId = CustomerId(value = ownerId),
        plate = LicensePlate.of(raw = plate),
        brand = brand,
        model = model,
        year = ModelYear.of(year = year),
        registeredAt = registeredAt.toKotlinInstant(),
    )
}
