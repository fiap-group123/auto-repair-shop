package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import br.com.autorepairshop.inputmanagment.domain.aggregate.Inventory
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryType
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class InventoryRepositoryImpl(private val jpa: InventoryJpaRepository) : InventoryRepository {

    override fun save(inventory: Inventory) {
        jpa.save(inventory.toEntity())
    }

    override fun findById(id: InventoryId): Inventory? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<Inventory> = jpa.findAll().map { it.toDomain() }

    override fun existsByName(name: InventoryName): Boolean = jpa.existsByName(name = name.value)

    private fun Inventory.toEntity() = InventoryEntity(
        id = id.value,
        name = name.value,
        kind = InventoryKindColumn.valueOf(value = type.name),
        unitPrice = unitPrice.amount,
        stock = stock,
        active = active,
        createdAt = createdAt.toJavaInstant(),
    )

    private fun InventoryEntity.toDomain() = Inventory.rehydrate(
        id = InventoryId(value = id),
        name = InventoryName.of(raw = name),
        type = InventoryType.valueOf(value = kind.name),
        unitPrice = Money.of(raw = unitPrice),
        stock = stock,
        active = active,
        createdAt = createdAt.toKotlinInstant(),
    )
}
