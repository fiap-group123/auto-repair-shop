package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListVehiclesByOwnerUseCase(
    private val customers: CustomerRepository,
    private val vehicles: VehicleRepository,
) : UseCase<UUID, List<VehicleResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<VehicleResponse> {
        val ownerId = CustomerId(value = input)
        customers.findById(id = ownerId)
            ?: throw CustomerException.CustomerNotFound(message = "Customer $input was not found.")
        return vehicles.findByOwner(ownerId = ownerId).map(transform = { it.toResponse() })
    }
}
