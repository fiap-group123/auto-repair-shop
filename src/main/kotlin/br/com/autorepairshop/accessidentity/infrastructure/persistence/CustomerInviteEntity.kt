package br.com.autorepairshop.accessidentity.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "customer_invites")
class CustomerInviteEntity(
    @Id
    val id: UUID,
    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    val tokenHash: String,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    @Column(name = "consumed_at")
    var consumedAt: Instant?,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
