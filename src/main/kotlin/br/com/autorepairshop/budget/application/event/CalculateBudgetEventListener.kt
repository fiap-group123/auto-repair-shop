package br.com.autorepairshop.budget.application.event

import br.com.autorepairshop.budget.application.usecase.CalculateBudgetTotalUseCase
import br.com.autorepairshop.catalog.domain.event.ExtraServiceApproved
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRejected
import br.com.autorepairshop.catalog.domain.event.ServicePriceChanged
import br.com.autorepairshop.catalog.domain.event.ServiceRegistered
import br.com.autorepairshop.catalog.domain.event.ServiceRemoved
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CalculateBudgetEventListener(private val calculateBudgetTotal: CalculateBudgetTotalUseCase) {
    @EventListener
    fun on(event: ServiceRegistered) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }

    @EventListener
    fun on(event: ServicePriceChanged) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }

    @EventListener
    fun on(event: ServiceRemoved) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }

    @EventListener
    fun on(event: ExtraServiceApproved) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }

    @EventListener
    fun on(event: ExtraServiceRejected) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }
}
