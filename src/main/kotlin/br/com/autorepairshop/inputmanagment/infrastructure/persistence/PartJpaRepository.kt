package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PartJpaRepository : JpaRepository<PartEntity, UUID> {
    fun existsByInventoryIdAndServiceOrderId(
        inventoryId: UUID,
        serviceOrderId: UUID,
    ): Boolean
    fun findAllByServiceOrderId(serviceOrderId: UUID): List<PartEntity>
    fun findAllByServiceOrderIdIn(serviceOrderIds: Collection<UUID>): List<PartEntity>
}
