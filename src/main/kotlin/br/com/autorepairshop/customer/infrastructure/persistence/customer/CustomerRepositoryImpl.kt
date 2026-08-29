package br.com.autorepairshop.customer.infrastructure.persistence.customer

import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import org.springframework.stereotype.Repository
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class CustomerRepositoryImpl(private val jpa: CustomerJpaRepository) : CustomerRepository {

    override fun save(customer: Customer) {
        jpa.save(customer.toEntity())
    }

    override fun findById(id: CustomerId): Customer? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByDocumentId(id: Document): Customer? = jpa.findByDocumentId(documentId = id.value)?.toDomain()

    override fun existsByDocumentId(id: Document): Boolean = jpa.existsByDocumentId(documentId = id.value)

    override fun findAll(): List<Customer> = jpa.findAll().map { it.toDomain() }

    private fun Customer.toEntity() = CustomerEntity(
        id = id.value,
        documentId = document.value,
        name = name.value,
        email = contact.email.value,
        phone = contact.phone.value,
        active = active,
        registeredAt = registeredAt.toJavaInstant(),
    )

    private fun CustomerEntity.toDomain() = Customer.rehydrate(
        id = CustomerId(value = id),
        documentId = Document.of(raw = documentId),
        name = PersonName.of(raw = name),
        contact = ContactInfo.of(
            email = email,
            phone = phone,
        ),
        active = active,
        registeredAt = registeredAt.toKotlinInstant(),
    )
}
