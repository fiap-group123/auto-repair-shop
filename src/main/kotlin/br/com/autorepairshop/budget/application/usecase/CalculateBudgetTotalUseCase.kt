package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CalculateBudgetTotalUseCase(
    private val orders: ServiceOrderRepository,
    private val services: ServiceRepository,
    private val budgets: BudgetRepository,
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        orders.findById(id = ServiceOrderId(value = input))
            ?: throw BudgetException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        val budget = budgets.findByServiceOrderId(serviceOrderId = input) ?: return
        val total = services.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, service -> acc.plus(other = service.basePrice) }
        budget.updateBudgetTotal(newTotal = total)
        budgets.save(budget)
    }
}
