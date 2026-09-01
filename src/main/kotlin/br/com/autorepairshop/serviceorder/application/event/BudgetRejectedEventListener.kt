package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.budget.domain.event.BudgetRejected
import br.com.autorepairshop.serviceorder.application.usecase.RejectServiceOrderUseCase
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BudgetRejectedEventListener(private val rejectOrder: RejectServiceOrderUseCase) {
    @EventListener
    fun on(event: BudgetRejected) {
        rejectOrder.execute(input = event.serviceOrderId)
    }
}
