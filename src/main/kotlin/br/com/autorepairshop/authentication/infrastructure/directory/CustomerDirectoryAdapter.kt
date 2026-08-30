package br.com.autorepairshop.authentication.infrastructure.directory

import br.com.autorepairshop.authentication.application.directory.CustomerDirectory
import br.com.autorepairshop.authentication.application.directory.CustomerRecord
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerDirectoryAdapter(private val customers: CustomerRepository) : CustomerDirectory {

    override fun find(id: UUID): CustomerRecord? {
        val customer = customers.findById(id = CustomerId(value = id)) ?: return null
        return CustomerRecord(
            id = customer.id.value,
            name = customer.name.value,
            email = customer.contact.email.value,
            active = customer.active,
        )
    }
}
