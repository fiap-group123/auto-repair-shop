package br.com.autorepairshop.shared.application.event

import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.DomainEvent

interface EventPublisher {
    fun publish(event: DomainEvent)
    fun publish(aggregate: AggregateRoot<*>)
}