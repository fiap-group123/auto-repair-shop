package br.com.autorepairshop.serviceandexecution.application.event

import br.com.autorepairshop.budget.domain.event.BudgetApproved
import br.com.autorepairshop.serviceandexecution.application.usecase.ApproveServiceOrderUseCase
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BudgetApprovedEventListener(private val approveOrder: ApproveServiceOrderUseCase) {
    @EventListener
    fun on(event: BudgetApproved) {
        approveOrder.execute(input = event.serviceOrderId)
    }
}
