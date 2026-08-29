package br.com.autorepairshop.catalog.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceJpaRepository : JpaRepository<ServiceEntity, UUID> {
    fun existsByNameAndServiceOrderId(
        name: String,
        serviceOrderId: UUID,
    ): Boolean
    fun findAllByServiceOrderId(serviceOrderId: UUID): List<ServiceEntity>
    fun findAllByServiceOrderIdIn(serviceOrderIds: Collection<UUID>): List<ServiceEntity>
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}
