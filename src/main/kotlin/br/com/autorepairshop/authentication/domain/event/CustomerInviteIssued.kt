package br.com.autorepairshop.authentication.domain.event

import br.com.autorepairshop.shared.domain.DomainEvent
import java.time.Instant
import java.util.UUID

data class CustomerInviteIssued(
    val customerId: UUID,
    val customerName: String,
    val contactEmail: String,
    val rawToken: String,
    override val occurredOn: Instant,
) : DomainEvent
