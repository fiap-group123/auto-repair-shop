package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReactivateCustomerUseCase(private val customers: CustomerRepository) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val customer = customers.findById(
            id = CustomerId(value = input),
        ) ?: throw CustomerException.CustomerNotFound(message = "Customer $input was not found.")
        customer.reactivate()
        customers.save(customer = customer)
    }
}
