package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.toResponse
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateCustomerUseCase(
    private val customers: CustomerRepository,
) : UseCase<UpdateCustomerCommand, CustomerResponse> {

    @Transactional
    override fun execute(input: UpdateCustomerCommand): CustomerResponse {
        val customer = customers.findById(CustomerId(input.customerId))
            ?: throw CustomerException.CustomerNotFound("Customer ${input.customerId} was not found.")

        customer.rename(PersonName.of(input.name))
        customer.updateContact(ContactInfo.of(input.email, input.phone))
        customers.save(customer)
        return customer.toResponse()
    }
}
