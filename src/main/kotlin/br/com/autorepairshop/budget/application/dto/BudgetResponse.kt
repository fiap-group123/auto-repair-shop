package br.com.autorepairshop.budget.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class BudgetResponse(
    val id: UUID,
    val serviceOrderId: UUID,
    val total: BigDecimal,
    val status: String,
    val createdAt: Instant,
    val finishedAt: Instant?,
)
