package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TradeBudgetUseCase(private val budgets: BudgetRepository): UseCase<UUID, BudgetResponse> {
    override fun execute(input: UUID): BudgetResponse {
        val budget = budgets.findByServiceOrderId(serviceOrderId = input)
            ?: throw BudgetException.BudgetNotFound(message = "Budget with order $input not found")

        budget.trade()
        budgets.save(budget)
        return budget.toResponse()
    }
}
