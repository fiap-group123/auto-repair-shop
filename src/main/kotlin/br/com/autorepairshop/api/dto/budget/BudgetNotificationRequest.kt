package br.com.autorepairshop.api.dto.budget

import java.util.UUID

data class BudgetNotificationRequest(
    val serviceOrderId: UUID,
    val decision: String,
)
