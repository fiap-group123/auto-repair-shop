package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindBudgetUseCase(
    private val budgets: BudgetRepository,
    private val orders: ServiceOrderRepository,
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
            ?: throw BudgetException.BudgetNotFound(message = "Budget of order $input was not found.")
        return budget.toResponse()
    }
}
