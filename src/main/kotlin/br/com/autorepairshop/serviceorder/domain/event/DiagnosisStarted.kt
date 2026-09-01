package br.com.autorepairshop.serviceorder.domain.event

import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class DiagnosisStarted(
    val serviceOrderId: UUID,
    override val occurredOn: Instant,
) : DomainEvent
