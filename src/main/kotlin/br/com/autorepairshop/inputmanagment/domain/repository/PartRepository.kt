package br.com.autorepairshop.inputmanagment.domain.repository

import br.com.autorepairshop.inputmanagment.domain.aggregate.Part
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import java.util.UUID

interface PartRepository {
    fun save(part: Part)
    fun findById(id: PartId): Part?
    fun existsByInventoryId(
        inventoryId: InventoryId,
        serviceOrderId: UUID,
    ): Boolean
    fun findByServiceOrderId(serviceOrderId: UUID): List<Part>
    fun findByServiceOrderIds(serviceOrderIds: Collection<UUID>): List<Part>
    fun delete(part: Part)
}
