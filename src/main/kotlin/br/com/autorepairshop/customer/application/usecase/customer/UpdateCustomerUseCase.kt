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
class UpdateCustomerUseCase(private val customers: CustomerRepository) :
    UseCase<UpdateCustomerCommand, CustomerResponse> {

    @Transactional
    override fun execute(input: UpdateCustomerCommand): CustomerResponse {
        val customer = customers.findById(id = CustomerId(value = input.customerId))
            ?: throw CustomerException.CustomerNotFound(
                message = "Customer ${input.customerId} was not found.",
            )

        input.name?.let(block = { customer.rename(newName = PersonName.of(raw = it)) })

        if (input.email != null || input.phone != null) {
            customer.updateContact(
                newContact = ContactInfo.of(
                    email = input.email ?: customer.contact.email.value,
                    phone = input.phone ?: customer.contact.phone.value,
                ),
            )
        }
        customers.save(customer = customer)
        return customer.toResponse()
    }
}
