package br.com.autorepairshop.shared.infrastructure.event

import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.DomainEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import kotlin.test.assertTrue

@Tag("unit")
class SpringEventPublisherTest {
    private val spring = mockk<ApplicationEventPublisher>()
    private val publisher = SpringEventPublisher(publisher = spring)

    @Test
    fun `forwards a single event`() {
        val event = TestEvent()
        every { spring.publishEvent(event) } just Runs

        publisher.publish(event = event)

        verify { spring.publishEvent(event) }
    }

    @Test
    fun `publishes pending aggregate events and clears them`() {
        val first = TestEvent()
        val second = TestEvent()
        val aggregate = TestAggregate()
        aggregate.record(event = first)
        aggregate.record(event = second)
        every { spring.publishEvent(first) } just Runs
        every { spring.publishEvent(second) } just Runs

        publisher.publish(aggregate = aggregate)

        verify { spring.publishEvent(first) }
        verify { spring.publishEvent(second) }
        assertTrue(actual = aggregate.domainEvents.isEmpty())
    }

    private class TestEvent(override val occurredOn: Instant = Instant.parse("2026-09-01T12:00:00Z")) : DomainEvent

    private class TestAggregate : AggregateRoot<String>(id = "root") {
        fun record(event: DomainEvent) = registerEvent(event = event)
    }
}
