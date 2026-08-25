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
        val vehicle = vehicles.findById(id = VehicleId(value = input.vehicleId))
            ?: throw VehicleException.VehicleNotFound(
                message = "Vehicle ${input.vehicleId} was not found.",
            )

        val newOwnerId = CustomerId(value = input.newOwnerId)
        val newOwner = customers.findById(id = newOwnerId)
            ?: throw CustomerException.CustomerNotFound(
                message = "Customer ${input.newOwnerId} was not found.",
            )
        if (!newOwner.active) {
            throw CustomerException.InvalidDocument(
                message = "Customer ${newOwner.documentId.masked()} is inactive.",
            )
        }

        vehicle.transferTo(newOwnerId = newOwnerId)
        vehicles.save(vehicle = vehicle)
        return vehicle.toResponse()
    }
}
