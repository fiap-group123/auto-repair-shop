package br.com.autorepairshop.serviceandexecution.domain.event

import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant

data class ServiceOrderCompleted(
    val serviceOrderId: ServiceOrderId,
    override val occurredOn: Instant,
) : DomainEvent
