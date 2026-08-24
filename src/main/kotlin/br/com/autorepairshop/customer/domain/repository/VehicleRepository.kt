package br.com.autorepairshop.customer.domain.repository

import br.com.autorepairshop.customer.domain.aggregate.Vehicle
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId

interface VehicleRepository {
    fun save(vehicle: Vehicle)
    fun findById(id: VehicleId): Vehicle?
    fun findByPlate(plate: LicensePlate): Vehicle?
    fun findByOwner(ownerId: CustomerId): List<Vehicle>
    fun existsByPlate(plate: LicensePlate): Boolean
}
