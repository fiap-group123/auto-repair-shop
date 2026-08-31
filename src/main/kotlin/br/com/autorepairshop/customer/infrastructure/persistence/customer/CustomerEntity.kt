package br.com.autorepairshop.customer.infrastructure.persistence.customer

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "customers")
class CustomerEntity(
    @Id
    val id: UUID,
    @Column(name = "document_id", nullable = false, unique = true, length = 14)
    val documentId: String,
    @Column(nullable = false, length = 60)
    var name: String,
    @Column(nullable = false, length = 60)
    var email: String,
    @Column(nullable = false, length = 11)
    var phone: String,
    @Column(nullable = false)
    var active: Boolean,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
