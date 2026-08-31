package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.toResponse
import br.com.autorepairshop.budget.domain.aggregate.Budget
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RegisterBudgetUseCase(private val budgets: BudgetRepository): UseCase<UUID, BudgetResponse>{

    @Transactional
    override fun execute(input: UUID): BudgetResponse {
        budgets.findByServiceOrderId(serviceOrderId = input)
            ?:  throw BudgetException.BudgetAlreadyExists(
                message = "Budget of order $input already exists."
            )

        val budget = Budget.register(
            serviceOrderId = input
        )

        budgets.save(budget)
        return budget.toResponse()
    }
}
