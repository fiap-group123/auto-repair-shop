package br.com.autorepairshop.customer.infrastructure.persistence.customer

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerJpaRepository : JpaRepository<CustomerEntity, UUID> {
    fun findByDocumentId(documentId: String): CustomerEntity?
    fun existsByDocumentId(documentId: String): Boolean
}
