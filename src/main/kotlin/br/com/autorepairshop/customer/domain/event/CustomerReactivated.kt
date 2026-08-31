package br.com.autorepairshop.customer.domain.event

import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class CustomerReactivated(
    val customerId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
