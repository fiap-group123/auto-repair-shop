package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.aggregate.Budget
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RegisterBudgetUseCase(
    private val budgets: BudgetRepository,
    private val orders: ServiceOrderRepository,
    private val services: ServiceRepository,
    private val parts: PartRepository,
) : UseCase<UUID, BudgetResponse> {

    @Transactional
    override fun execute(input: UUID): BudgetResponse {
        orders.findById(id = ServiceOrderId(value = input))
            ?: throw BudgetException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        if (budgets.findByServiceOrderId(serviceOrderId = input) != null) {
            throw BudgetException.BudgetAlreadyExists(
                message = "Budget of order $input already exists.",
            )
        }
        val serviceTotal = services.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, service -> acc.plus(other = service.basePrice) }
        val partTotal = parts.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, part -> acc.plus(other = part.lineTotal()) }
        val total = serviceTotal.plus(other = partTotal)
        val budget = Budget.register(
            serviceOrderId = input,
            total = total,
        )
        budgets.save(budget)
        return budget.toResponse()
    }
}
