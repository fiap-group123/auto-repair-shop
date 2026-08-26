package br.com.autorepairshop.serviceorder.infrastructure.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
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
    @Column(name = "opened_at", nullable = false)
    val openedAt: Instant,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "service_order_items",
        joinColumns = [JoinColumn(name = "service_order_id")],
    )
    var items: MutableList<ServiceOrderItemColumn> = mutableListOf(),
    @Column(name = "diagnosis_started_at")
    var diagnosisStartedAt: Instant? = null,
    @Column(name = "diagnosis_finished_at")
    var diagnosisFinishedAt: Instant? = null,
    @Column(name = "approved_at")
    var approvedAt: Instant? = null,
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
    @Column(name = "delivered_at")
    var deliveredAt: Instant? = null,
)
