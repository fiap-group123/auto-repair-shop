package br.com.autorepairshop.serviceorder.infrastructure.persistence

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
@Table(name = "service_orders")
class ServiceOrderEntity(
    @Id
    val id: UUID,
    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,
    @Column(name = "vehicle_id", nullable = false)
    val vehicleId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ServiceOrderStatusColumn,
    @Column(nullable = false, precision = 10, scale = 2)
    var total: BigDecimal,
    @Column(name = "registered_at", nullable = false)
    val registeredAt: Instant,
    @Column(name = "opened_at")
    var openedAt: Instant?,
    @Column(name = "finished_at")
    var finishedAt: Instant?,
    @Column(name = "estimate_time_seconds")
    var estimateTimeSeconds: Long?,
)
