package br.com.autorepairshop.budget.application.event

import br.com.autorepairshop.budget.application.usecase.RegisterBudgetUseCase
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class RegisterBudgetEventListener(private val registerBudget: RegisterBudgetUseCase) {
    @EventListener
    fun on(event: DiagnosisStarted) {
        registerBudget.execute(input = event.serviceOrderId)
    }
}
