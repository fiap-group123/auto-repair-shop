package br.com.autorepairshop.customer

import br.com.autorepairshop.customer.exception.CustomerException
import br.com.autorepairshop.customer.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.valueobject.customer.PersonName
import br.com.autorepairshop.customer.valueobject.document.DocumentId
import br.com.autorepairshop.shared.domain.AggregateRoot
import kotlin.time.Clock
import kotlin.time.Instant

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
        // registerEvent(CustomerDeactivated(id.value, at))
    }
    fun reactivate() {
        if (active) throw CustomerException.CustomerAlreadyExists("Customer is already active")
        active = true
    }

    private fun requireActive() {
        if (!active) throw CustomerException.InvalidDocument("Customer ${documentId.masked()} is inactive")
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
            name = name, contact,
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