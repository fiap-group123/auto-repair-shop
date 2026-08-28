package br.com.autorepairshop.customer.domain.aggregate

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import br.com.autorepairshop.shared.domain.Entity
import kotlin.time.Clock
import kotlin.time.Instant

class Customer private constructor(
    id: CustomerId,
    val document: Document,
    name: PersonName,
    contact: ContactInfo,
    active: Boolean,
    val registeredAt: Instant,
) : Entity<CustomerId>(id = id) {

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

    fun deactivate() {
        requireActive()
        active = false
    }

    fun reactivate() {
        if (active) throw CustomerException.CustomerAlreadyActive(message = "Customer is already active.")
        active = true
    }

    private fun requireActive() {
        if (!active) {
            throw CustomerException.InvalidDocument(
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
            registeredAt = at,
        )

        internal fun rehydrate(
            id: CustomerId,
            documentId: Document,
            name: PersonName,
            contact: ContactInfo,
            active: Boolean,
            registeredAt: Instant,
        ) = Customer(
            id = id,
            document = documentId,
            name = name,
            contact = contact,
            active = active,
            registeredAt = registeredAt,
        )
    }
}
