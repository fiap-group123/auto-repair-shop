package br.com.autorepairshop.budget.domain.repositories

import br.com.autorepairshop.budget.domain.aggregate.Budget
import java.util.UUID

interface BudgetRepository {
    fun save(budget: Budget)
    fun findByServiceOrderId(serviceOrderId: UUID): Budget?
    fun deleteByServiceOrderId(serviceOrderId: UUID)
}
