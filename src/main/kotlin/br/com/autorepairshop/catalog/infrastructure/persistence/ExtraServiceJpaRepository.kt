package br.com.autorepairshop.catalog.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExtraServiceJpaRepository : JpaRepository<ExtraServiceEntity, UUID> {
    fun existsByNameAndServiceOrderId(
        name: String,
        serviceOrderId: UUID,
    ): Boolean
    fun findAllByServiceOrderId(serviceOrderId: UUID): List<ExtraServiceEntity>
}
