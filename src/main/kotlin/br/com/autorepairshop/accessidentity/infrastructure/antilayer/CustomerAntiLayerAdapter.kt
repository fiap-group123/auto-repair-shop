package br.com.autorepairshop.accessidentity.infrastructure.antilayer

import br.com.autorepairshop.accessidentity.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.accessidentity.application.antilayer.CustomerRecord
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerAntiLayerAdapter(private val customers: CustomerRepository) : CustomerAntiLayer {

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
