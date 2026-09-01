package br.com.autorepairshop.catalog.domain.event

import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class ExtraServiceRegistered(
    val extraServiceId: ExtraServiceId,
    val serviceOrderId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
