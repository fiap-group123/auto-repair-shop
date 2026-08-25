package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.toResponse
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindCustomerUseCase(private val customers: CustomerRepository) : UseCase<UUID, CustomerResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): CustomerResponse {
        val customer = customers.findById(id = CustomerId(value = input))
            ?: throw CustomerException.CustomerNotFound(message = "Customer $input was not found.")
        return customer.toResponse()
    }
}
