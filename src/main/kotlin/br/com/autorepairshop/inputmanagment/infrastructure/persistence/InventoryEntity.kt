package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "inventories")
class InventoryEntity(
    @Id
    val id: UUID,
    @Column(nullable = false, length = 60, unique = true)
    var name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var kind: InventoryKindColumn,
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,
    @Column(nullable = false)
    var stock: Int,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
