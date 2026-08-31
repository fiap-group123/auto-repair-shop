package br.com.autorepairshop.budget.infrastructure.persistence

import br.com.autorepairshop.budget.domain.aggregate.Budget
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.budget.domain.valueObject.BudgetId
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class BudgetRepositoryImpl(private val jpa: BudgetJpaRepository) : BudgetRepository {

    override fun save(budget: Budget) {
        jpa.save(budget.toEntity())
    }

    override fun findByServiceOrderId(serviceOrderId: UUID): Budget? =
        jpa.findByServiceOrderId(serviceOrderId = serviceOrderId).toDomain()

    override fun deleteByServiceOrderId(serviceOrderId: UUID) =
        jpa.deleteByServiceOrderId(serviceOrderId = serviceOrderId)


    private fun Budget.toEntity() = BudgetEntity(
        id = id.value,
        serviceOrderId = serviceOrderId,
        total = total.amount,
        status = BudgetStatusColumn.valueOf(value = status.name),
        createdAt = createdAt.toJavaInstant(),
        finishedAt = finishedAt?.toJavaInstant(),
    )

    private fun BudgetEntity.toDomain() = Budget.rehydrate(
        id = BudgetId(value = id),
        serviceOrderId = serviceOrderId,
        total = Money.of(raw = total),
        status = BudgetStatus.valueOf(value = status.name),
        createdAt = createdAt.toKotlinInstant(),
        finishedAt = finishedAt?.toKotlinInstant()
    )
}
