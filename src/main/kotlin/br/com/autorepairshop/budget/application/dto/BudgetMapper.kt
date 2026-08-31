package br.com.autorepairshop.budget.application.dto

import br.com.autorepairshop.budget.domain.aggregate.Budget

fun Budget.toResponse() = BudgetResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    total = total,
    status = status,
    createdAt = createdAt,
    finishedAt = finishedAt
)
