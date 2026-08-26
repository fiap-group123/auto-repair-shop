package br.com.autorepairshop.catalog.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    @Column(nullable = false, unique = true, length = 60)
    var name: String,
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "registered_at", nullable = false)
    val registeredAt: Instant,
)
