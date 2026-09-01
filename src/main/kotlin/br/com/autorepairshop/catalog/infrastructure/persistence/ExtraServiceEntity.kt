package br.com.autorepairshop.catalog.infrastructure.persistence

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
@Table(name = "extra_services")
class ExtraServiceEntity(
    @Id
    val id: UUID,
    @Column(name = "service_order_id", nullable = false)
    val serviceOrderId: UUID,
    @Column(nullable = false, length = 60)
    var name: String,
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ExtraServiceStatusColumn,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
