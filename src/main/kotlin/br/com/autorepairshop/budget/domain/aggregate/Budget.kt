package br.com.autorepairshop.budget.domain.aggregate

import br.com.autorepairshop.budget.domain.event.BudgetApproved
import br.com.autorepairshop.budget.domain.event.BudgetRejected
import br.com.autorepairshop.budget.domain.event.BudgetTraded
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.valueObject.BudgetId
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class Budget private constructor(
    id: BudgetId,
    status: BudgetStatus,
    val serviceOrderId: UUID,
    total: Money,
    createdAt: Instant,
    finishedAt: Instant?,
) : AggregateRoot<BudgetId>(id = id) {
    var total: Money = total
        private set

    var status: BudgetStatus = status
        private set

    var createdAt: Instant = createdAt
        private set

    var finishedAt: Instant? = finishedAt
        private set

    fun updateBudgetTotal(newTotal: Money) {
        total = newTotal
    }

    private fun checkStatus() {
        if (status != BudgetStatus.WAITING_APPROVAL) {
            throw BudgetException.InvalidBudgetStatusTransition(
                message = "Cannot transition from ${status.name}.",
            )
        }
    }

    private fun changeStatus(newStatus: BudgetStatus) {
        checkStatus()
        status = newStatus
    }

    fun approve(at: Instant = Clock.System.now()) {
        changeStatus(newStatus = BudgetStatus.APPROVED)
        finishedAt = at
        registerEvent(
            event = BudgetApproved(
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun reject(at: Instant = Clock.System.now()) {
        changeStatus(newStatus = BudgetStatus.REJECTED)
        finishedAt = at
        registerEvent(
            event = BudgetRejected(
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun trade(at: Instant = Clock.System.now()) {
        changeStatus(newStatus = BudgetStatus.TRADED)
        finishedAt = at
        registerEvent(
            event = BudgetTraded(
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    companion object {
        fun register(
            serviceOrderId: UUID,
            createdAt: Instant = Clock.System.now(),
            finishedAt: Instant? = null,
            total: Money = Money.ZERO,
        ) = Budget(
            id = BudgetId.generate(),
            status = BudgetStatus.WAITING_APPROVAL,
            total = total,
            createdAt = createdAt,
            serviceOrderId = serviceOrderId,
            finishedAt = finishedAt,
        )

        internal fun rehydrate(
            id: BudgetId,
            serviceOrderId: UUID,
            total: Money,
            status: BudgetStatus,
            createdAt: Instant,
            finishedAt: Instant?,
        ) = Budget(
            id = id,
            serviceOrderId = serviceOrderId,
            total = total,
            status = status,
            createdAt = createdAt,
            finishedAt = finishedAt,
        )
    }
}
