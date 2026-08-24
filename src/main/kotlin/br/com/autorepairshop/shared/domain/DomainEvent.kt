package br.com.autorepairshop.shared.domain

import java.time.Instant

interface DomainEvent {
    val occurredOn: Instant
}
