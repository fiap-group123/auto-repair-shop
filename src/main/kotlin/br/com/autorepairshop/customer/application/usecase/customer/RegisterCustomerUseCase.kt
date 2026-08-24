package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.toResponse
import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.DocumentId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterCustomerUseCase(
    private val customers: CustomerRepository,
) : UseCase<RegisterCustomerCommand, CustomerResponse> {

    @Transactional
    override fun execute(input: RegisterCustomerCommand): CustomerResponse {
        val documentId = DocumentId.of(input.documentId)
        if (customers.existsByDocumentId(documentId)) {
            throw CustomerException.CustomerAlreadyExists(
                "Customer with document ${documentId.masked()} already exists."
            )
        }
        val customer = Customer.register(
            documentId = documentId,
            name = PersonName.of(input.name),
            contact = ContactInfo.of(input.email, input.phone),
        )
        customers.save(customer)
        return customer.toResponse()
    }
}
