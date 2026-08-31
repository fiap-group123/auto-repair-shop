package br.com.autorepairshop.budget.domain.valueObject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class BudgetId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): BudgetId = BudgetId(UUID.randomUUID())
    }
}
