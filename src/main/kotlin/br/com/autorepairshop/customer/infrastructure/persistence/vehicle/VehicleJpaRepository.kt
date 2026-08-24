package br.com.autorepairshop.customer.infrastructure.persistence.vehicle

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface VehicleJpaRepository : JpaRepository<VehicleEntity, UUID> {
    fun findByPlate(plate: String): VehicleEntity?
    fun existsByPlate(plate: String): Boolean
    fun findAllByOwnerId(ownerId: UUID): List<VehicleEntity>
}
