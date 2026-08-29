package br.com.autorepairshop.catalog.domain.event

import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class ServiceRemoved(
    val serviceId: ServiceId,
    val serviceOrderId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
