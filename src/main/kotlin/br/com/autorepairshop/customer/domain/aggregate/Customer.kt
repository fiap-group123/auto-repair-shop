package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.domain.event.CustomerDeactivated
import br.com.autorepairshop.customer.domain.event.CustomerReactivated
import br.com.autorepairshop.customer.domain.event.CustomerRegistered
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import br.com.autorepairshop.shared.domain.AggregateRoot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class Customer private constructor(
    id: CustomerId,
    val document: Document,
    name: PersonName,
    contact: ContactInfo,
    active: Boolean,
    val createdAt: Instant,
) : AggregateRoot<CustomerId>(id = id) {

    var name: PersonName = name
        private set

    var contact: ContactInfo = contact
        private set

    var active: Boolean = active
        private set

    fun rename(newName: PersonName) {
        requireActive()
        name = newName
    }

    fun updateContact(newContact: ContactInfo) {
        requireActive()
        contact = newContact
    }

    fun deactivate(at: Instant = Clock.System.now()) {
        requireActive()
        active = false
        registerEvent(
            event = CustomerDeactivated(
                customerId = id.value,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun reactivate(at: Instant = Clock.System.now()) {
        if (active) throw CustomerException.CustomerAlreadyActive(message = "Customer is already active.")
        active = true
        registerEvent(
            event = CustomerReactivated(
                customerId = id.value,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    private fun requireActive() {
        if (!active) {
            throw CustomerException.CustomerInactive(
                message = "Customer ${document.masked()} is inactive.",
            )
        }
    }

    companion object {
        fun register(
            documentId: Document,
            name: PersonName,
            contact: ContactInfo,
            at: Instant = Clock.System.now(),
        ) = Customer(
            id = CustomerId.generate(),
            document = documentId,
            name = name,
            contact = contact,
            active = true,
            createdAt = at,
        ).apply {
            registerEvent(
                event = CustomerRegistered(
                    customerId = id.value,
                    occurredOn = createdAt.toJavaInstant(),
                ),
            )
        }

        internal fun rehydrate(
            id: CustomerId,
            documentId: Document,
            name: PersonName,
            contact: ContactInfo,
            active: Boolean,
            createdAt: Instant,
        ) = Customer(
            id = id,
            document = documentId,
            name = name,
            contact = contact,
            active = active,
            createdAt = createdAt,
        )
    }
}
