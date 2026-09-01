package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.budget.domain.event.BudgetTraded
import br.com.autorepairshop.serviceorder.application.usecase.ApproveServiceOrderUseCase
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BudgetTradedEventListener(private val approveOrder: ApproveServiceOrderUseCase) {
    @EventListener
    fun on(event: BudgetTraded) {
        approveOrder.execute(input = event.serviceOrderId)
    }
}
