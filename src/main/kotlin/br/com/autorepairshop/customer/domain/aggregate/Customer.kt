package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.domain.event.CustomerDeactivated
import br.com.autorepairshop.customer.domain.event.CustomerReactivated
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.DocumentId
import br.com.autorepairshop.shared.domain.AggregateRoot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class Customer private constructor(
    id: CustomerId,
    val documentId: DocumentId,
    name: PersonName,
    contact: ContactInfo,
    active: Boolean,
    val registeredAt: Instant,
) : AggregateRoot<CustomerId>(id){

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

    fun updateContact(newContact: ContactInfo){
        requireActive()
        contact = newContact
    }

    fun deactivate(at: Instant = Clock.System.now()) {
        requireActive()
        active = false
        registerEvent(
            CustomerDeactivated(
                customerId = id,
                occurredOn = at.toJavaInstant()
            )
        )
    }

    fun reactivate(at: Instant = Clock.System.now()) {
        if (active) throw CustomerException.CustomerAlreadyActive(message = "Customer is already active.")
        active = true
        registerEvent(
            CustomerReactivated(
                customerId = id,
                occurredOn = at.toJavaInstant()
            )
        )
    }

    private fun requireActive() {
        if (!active) throw CustomerException.InvalidDocument(message = "Customer ${documentId.masked()} is inactive.")
    }
    
    companion object {
        fun register(
            documentId: DocumentId,
            name: PersonName,
            contact: ContactInfo,
            at: Instant = Clock.System.now(),
        ) = Customer(
            id = CustomerId.generate(),
            documentId = documentId,
            name = name, 
            contact = contact,
            active = true,
            registeredAt = at
        )

        internal fun rehydrate(
            id: CustomerId,
            documentId: DocumentId,
            name: PersonName,
            contact: ContactInfo,
            active: Boolean,
            registeredAt: Instant,
        ) = Customer(
            id = id,
            documentId = documentId,
            name = name,
            contact = contact,
            active = active,
            registeredAt = registeredAt
        )
    }
}