package br.com.autorepairshop.budget.application.dto

import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.shared.domain.Money
import kotlin.time.Instant
import java.util.UUID

data class BudgetResponse (
    val id: UUID,
    val serviceOrderId: UUID,
    val total: Money,
    val status: BudgetStatus,
    val createdAt: Instant,
    val finishedAt: Instant?
)
