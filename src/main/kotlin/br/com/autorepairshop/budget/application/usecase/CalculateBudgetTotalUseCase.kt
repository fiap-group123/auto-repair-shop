package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceStatus
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CalculateBudgetTotalUseCase(
    private val orders: ServiceOrderRepository,
    private val services: ServiceRepository,
    private val extras: ExtraServiceRepository,
    private val parts: PartRepository,
    private val budgets: BudgetRepository,
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        orders.findById(id = ServiceOrderId(value = input))
            ?: throw BudgetException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        val budget = budgets.findByServiceOrderId(serviceOrderId = input) ?: return
        val serviceTotal = services.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, service -> acc.plus(other = service.basePrice) }
        val extraTotal = extras.findByServiceOrderId(serviceOrderId = input)
            .filter { extra -> extra.status in billedStatuses }
            .fold(initial = Money.ZERO) { acc, extra -> acc.plus(other = extra.basePrice) }
        val partTotal = parts.findByServiceOrderId(serviceOrderId = input)
            .fold(initial = Money.ZERO) { acc, part -> acc.plus(other = part.lineTotal()) }
        budget.updateBudgetTotal(newTotal = serviceTotal.plus(other = extraTotal).plus(other = partTotal))
        budgets.save(budget)
    }

    private companion object {
        val billedStatuses = setOf(
            ExtraServiceStatus.APPROVED,
            ExtraServiceStatus.IN_PROGRESS,
            ExtraServiceStatus.FINISHED,
        )
    }
}
