package br.com.autorepairshop.authentication.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID,
    @Column(nullable = false, unique = true, length = 60)
    val email: String,
    @Column(name = "password_hash", nullable = false, length = 100)
    var passwordHash: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: UserRoleColumn,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "customer_id")
    val customerId: UUID?,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
