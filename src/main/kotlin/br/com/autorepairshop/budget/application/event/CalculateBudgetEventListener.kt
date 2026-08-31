package br.com.autorepairshop.budget.application.event

import br.com.autorepairshop.catalog.domain.event.ServicePriceChanged
import br.com.autorepairshop.catalog.domain.event.ServiceRegistered
import br.com.autorepairshop.serviceorder.application.usecase.CalculateBudgetTotalUseCase
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Application reaction to catalog events: keep the order total aligned with its services.
 *
 * A plain [EventListener] runs inside the publishing transaction, so the total commits together with the
 * change that triggered it.
 */
@Component
class CalculateBudgetEventListener(
    private val calculateBudgetTotal: CalculateBudgetTotalUseCase,
) {
    @EventListener
    fun on(event: ServiceRegistered) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }

    @EventListener
    fun on(event: ServicePriceChanged) {
        calculateBudgetTotal.execute(input = event.serviceOrderId)
    }
}
