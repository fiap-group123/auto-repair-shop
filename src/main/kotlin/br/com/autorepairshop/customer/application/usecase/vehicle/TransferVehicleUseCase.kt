package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.TransferVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransferVehicleUseCase(
    private val customers: CustomerRepository,
    private val vehicles: VehicleRepository,
) : UseCase<TransferVehicleCommand, VehicleResponse> {

    @Transactional
    override fun execute(input: TransferVehicleCommand): VehicleResponse {
        val vehicle = vehicles.findById(VehicleId(input.vehicleId))
            ?: throw VehicleException.VehicleNotFound("Vehicle ${input.vehicleId} was not found.")

        val newOwnerId = CustomerId(input.newOwnerId)
        val newOwner = customers.findById(newOwnerId)
            ?: throw CustomerException.CustomerNotFound("Customer ${input.newOwnerId} was not found.")
        if (!newOwner.active) {
            throw CustomerException.InvalidDocument(
                "Customer ${newOwner.documentId.masked()} is inactive."
            )
        }

        vehicle.transferTo(newOwnerId)
        vehicles.save(vehicle)
        return vehicle.toResponse()
    }
}
