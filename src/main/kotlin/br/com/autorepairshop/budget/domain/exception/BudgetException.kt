package br.com.autorepairshop.budget.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class BudgetException(message: String) : DomainException(message = message) {
    class ServiceOrderNotFound(message: String) : BudgetException(message)
    class EmptyBudget(message: String) : BudgetException(message)
    class BudgetNotFound(message: String) : BudgetException(message)
    class InvalidBudgetStatusTransition(message: String) : BudgetException(message)
    class BudgetAlreadyExists(message: String) : BudgetException(message)
}
