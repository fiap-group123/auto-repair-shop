package br.com.autorepairshop.budget.infrastructure.persistence

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
@Table(name = "budgets")
class BudgetEntity(
    @Id
    val id: UUID,
    @Column(name = "service_order_id", nullable = false)
    val serviceOrderId: UUID,
    @Column(nullable = false, precision = 10, scale = 2)
    val total: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: BudgetStatusColumn,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "finished_at", nullable = true)
    val finishedAt: Instant?,
)
