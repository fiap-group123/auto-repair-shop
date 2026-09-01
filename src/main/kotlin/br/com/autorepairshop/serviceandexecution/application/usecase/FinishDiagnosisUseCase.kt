package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FinishDiagnosisUseCase(
    private val orders: ServiceOrderRepository,
    private val budgets: BudgetRepository,
    private val events: EventPublisher,
    private val responses: ServiceOrderAssembler,
) : UseCase<UUID, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: UUID): ServiceOrderResponse {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        val budget = budgets.findByServiceOrderId(serviceOrderId = input)
        if (budget == null || budget.total <= Money.ZERO) {
            throw BudgetException.EmptyBudget(
                message = "Cannot send an empty budget for approval.",
            )
        }
        order.finishDiagnosis()
        orders.save(order = order)
        events.publish(aggregate = order)
        return responses.toResponse(order = order)
    }
}
