package br.com.autorepairshop.budget.domain.event

import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class BudgetApproved(
    val serviceOrderId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
