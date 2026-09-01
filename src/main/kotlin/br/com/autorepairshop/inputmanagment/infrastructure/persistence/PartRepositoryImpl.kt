package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import br.com.autorepairshop.inputmanagment.domain.aggregate.Part
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class PartRepositoryImpl(private val jpa: PartJpaRepository) : PartRepository {

    override fun save(part: Part) {
        jpa.save(part.toEntity())
    }

    override fun findById(id: PartId): Part? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun existsByInventoryId(
        inventoryId: InventoryId,
        serviceOrderId: UUID,
    ): Boolean = jpa.existsByInventoryIdAndServiceOrderId(
        inventoryId = inventoryId.value,
        serviceOrderId = serviceOrderId,
    )

    override fun findByServiceOrderId(serviceOrderId: UUID): List<Part> =
        jpa.findAllByServiceOrderId(serviceOrderId).map { it.toDomain() }

    override fun findByServiceOrderIds(serviceOrderIds: Collection<UUID>): List<Part> {
        if (serviceOrderIds.isEmpty()) return emptyList()
        return jpa.findAllByServiceOrderIdIn(serviceOrderIds).map { it.toDomain() }
    }

    override fun delete(part: Part) {
        jpa.deleteById(part.id.value)
    }

    private fun Part.toEntity() = PartEntity(
        id = id.value,
        serviceOrderId = serviceOrderId,
        inventoryId = inventoryId.value,
        quantity = quantity,
        unitPrice = unitPrice.amount,
        createdAt = createdAt.toJavaInstant(),
    )

    private fun PartEntity.toDomain() = Part.rehydrate(
        id = PartId(value = id),
        serviceOrderId = serviceOrderId,
        inventoryId = InventoryId(value = inventoryId),
        quantity = quantity,
        unitPrice = Money.of(raw = unitPrice),
        createdAt = createdAt.toKotlinInstant(),
    )
}
