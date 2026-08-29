package br.com.autorepairshop.customer.domain.repository

import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.document.Document

interface CustomerRepository {
    fun save(customer: Customer)
    fun findById(id: CustomerId): Customer?
    fun findByDocumentId(id: Document): Customer?
    fun existsByDocumentId(id: Document): Boolean
    fun findAll(): List<Customer>
}
