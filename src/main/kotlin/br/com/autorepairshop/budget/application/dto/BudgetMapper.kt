package br.com.autorepairshop.budget.application.dto

import br.com.autorepairshop.budget.domain.aggregate.Budget
import kotlin.time.toJavaInstant

fun Budget.toResponse() = BudgetResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    total = total.amount,
    status = status.name,
    createdAt = createdAt.toJavaInstant(),
    finishedAt = finishedAt?.toJavaInstant(),
)
