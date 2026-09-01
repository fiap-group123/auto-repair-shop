package br.com.autorepairshop.inputmanagment.domain.event

import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class PartRegistered(
    val partId: PartId,
    val serviceOrderId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
