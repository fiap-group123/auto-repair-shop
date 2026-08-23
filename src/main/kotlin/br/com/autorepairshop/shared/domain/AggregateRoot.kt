package br.com.autorepairshop.shared.domain

abstract class AggregateRoot<ID : Any>(id: ID) : Entity<ID>(id = id) {
    private val events = mutableListOf<DomainEvent>()

    val domainEvents: List<DomainEvent> get() = events.toList()

    protected fun registerEvent(event: DomainEvent) {
        events.add(event)
    }

    fun clearEvents() = events.clear()
}
