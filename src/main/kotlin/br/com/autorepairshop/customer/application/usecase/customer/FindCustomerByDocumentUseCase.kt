package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.toResponse
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindCustomerByDocumentUseCase(private val customers: CustomerRepository) : UseCase<String, CustomerResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: String): CustomerResponse {
        val documentId = Document.of(raw = input)
        val customer = customers.findByDocumentId(id = documentId)
            ?: throw CustomerException.CustomerNotFound(
                message = "Customer with document ${documentId.masked()} was not found.",
            )
        return customer.toResponse()
    }
}
