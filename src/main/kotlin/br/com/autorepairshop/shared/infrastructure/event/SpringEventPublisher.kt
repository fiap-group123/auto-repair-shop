package br.com.autorepairshop.shared.infrastructure.event

import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisher(private val publisher: ApplicationEventPublisher) : EventPublisher {
    override fun publish(event: DomainEvent) = publisher.publishEvent(event)

    override fun publish(aggregate: AggregateRoot<*>) {
        aggregate.domainEvents.forEach { publish(event = it) }
        aggregate.clearEvents()
    }
}
