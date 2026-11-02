package br.com.autorepairshop.budget.application.dto

import java.util.UUID

data class ReceiveBudgetDecisionCommand(
    val serviceOrderId: UUID,
    val decision: String,
)
