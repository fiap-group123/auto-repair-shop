package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.budget.application.dto.BudgetResponse
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
import java.util.UUID

@Service
class ApproveBudgetUseCase(
    private val budgets: BudgetRepository,
    private val orders: ServiceOrderRepository,
    private val events: EventPublisher,
    private val access: AccessGuard,
) : UseCase<UUID, BudgetResponse> {

    @Transactional
    override fun execute(input: UUID): BudgetResponse {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        val budget = budgets.findByServiceOrderId(serviceOrderId = input)
            ?: throw BudgetException.BudgetNotFound(message = "Budget with order $input not found")
        budget.approve()
        budgets.save(budget)
        events.publish(aggregate = budget)
        return budget.toResponse()
    }
}
