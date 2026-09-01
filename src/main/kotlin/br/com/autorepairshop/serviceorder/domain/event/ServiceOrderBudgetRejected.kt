package br.com.autorepairshop.serviceorder.domain.event

import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant

data class ServiceOrderBudgetRejected(
    val serviceOrderId: ServiceOrderId,
    override val occurredOn: Instant,
) : DomainEvent
