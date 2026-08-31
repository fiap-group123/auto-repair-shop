package br.com.autorepairshop.budget

import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.aggregate.Budget
import br.com.autorepairshop.shared.domain.Money
import java.math.BigDecimal
import java.util.UUID

object BudgetFixtures {
    val TOTAL: Money = Money.of(raw = BigDecimal("150.00"))

    fun waitingApproval(
        serviceOrderId: UUID = UUID.randomUUID(),
        total: Money = TOTAL,
    ): Budget = Budget.register(
        serviceOrderId = serviceOrderId,
        total = total,
    )

    fun pricedResponse(
        serviceOrderId: UUID = UUID.randomUUID(),
        total: Money = TOTAL,
    ) = waitingApproval(serviceOrderId = serviceOrderId, total = total).toResponse()
}
