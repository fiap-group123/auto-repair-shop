package br.com.autorepairshop.budget.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BudgetJpaRepository: JpaRepository<BudgetEntity, UUID> {
    fun findByServiceOrderId(serviceOrderId: UUID): BudgetEntity
    fun deleteByServiceOrderId(serviceOrderId: UUID)
}
