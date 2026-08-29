package br.com.autorepairshop.customer.infrastructure.persistence.vehicle

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "vehicles")
class VehicleEntity(
    @Id
    val id: UUID,
    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,
    @Column(nullable = false, unique = true, length = 7)
    var plate: String,
    @Column(nullable = false, length = 40)
    var brand: String,
    @Column(nullable = false, length = 40)
    var model: String,
    @Column(nullable = false, length = 16)
    var color: String,
    @Column(nullable = false)
    var year: Int,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
