package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "parts")
class PartEntity(
    @Id
    val id: UUID,
    @Column(name = "service_order_id", nullable = false)
    val serviceOrderId: UUID,
    @Column(name = "inventory_id", nullable = false)
    val inventoryId: UUID,
    @Column(nullable = false)
    var quantity: Int,
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
