package br.com.autorepairshop.serviceorder.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceOrderJpaRepository : JpaRepository<ServiceOrderEntity, UUID> {
    fun findAllByCustomerId(customerId: UUID): List<ServiceOrderEntity>
    fun existsByVehicleIdAndStatusNot(
        vehicleId: UUID,
        status: ServiceOrderStatusColumn,
    ): Boolean
}
