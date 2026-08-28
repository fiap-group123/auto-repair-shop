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
@Table(name = "offered_services")
class OfferedServiceEntity(
    @Id
    val id: UUID,
    @Column(name = "service_order_id", nullable = false)
    val serviceOrderId: UUID,
    @Column(nullable = false, length = 60)
    var name: String,
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ServiceStatusColumn,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "registered_at", nullable = false)
    val registeredAt: Instant,
    @Column(name = "opened_at")
    var openedAt: Instant?,
    @Column(name = "finished_at")
    var finishedAt: Instant?,
    @Column(name = "estimated_time_seconds")
    var estimatedTimeSeconds: Long?,
)
