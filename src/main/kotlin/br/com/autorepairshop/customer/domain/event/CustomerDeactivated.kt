package br.com.autorepairshop.customer.domain.event

import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant

data class CustomerDeactivated(
    val customerId: CustomerId,
    override val occurredOn: Instant
) : DomainEvent
