package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.ReceiveBudgetDecisionCommand
import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReceiveBudgetDecisionUseCase(
    private val budgets: BudgetRepository,
    private val orders: ServiceOrderRepository,
    private val events: EventPublisher,
) : UseCase<ReceiveBudgetDecisionCommand, BudgetResponse> {

    @Transactional
    override fun execute(input: ReceiveBudgetDecisionCommand): BudgetResponse {
        val order = orders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        val budget = budgets.findByServiceOrderId(serviceOrderId = order.id.value)
            ?: throw BudgetException.BudgetNotFound(
                message = "Budget with order ${order.id.value} not found",
            )
        when (input.decision.trim().uppercase()) {
            "APPROVED" -> budget.approve()

            "REJECTED" -> budget.reject()

            else -> throw BudgetException.InvalidBudgetStatusTransition(
                message = "Decision must be APPROVED or REJECTED.",
            )
        }
        budgets.save(budget = budget)
        events.publish(aggregate = budget)
        return budget.toResponse()
    }
}
