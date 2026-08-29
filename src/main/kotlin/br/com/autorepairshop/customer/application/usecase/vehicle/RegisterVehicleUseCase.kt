package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.aggregate.Vehicle
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterVehicleUseCase(
    private val customers: CustomerRepository,
    private val vehicles: VehicleRepository,
) : UseCase<RegisterVehicleCommand, VehicleResponse> {

    @Transactional
    override fun execute(input: RegisterVehicleCommand): VehicleResponse {
        val ownerId = CustomerId(value = input.ownerId)
        val owner = customers.findById(id = ownerId)
            ?: throw CustomerException.CustomerNotFound(
                message = "Customer ${input.ownerId} was not found.",
            )
        if (!owner.active) {
            throw CustomerException.InvalidDocument(
                message = "Customer ${owner.document.masked()} is inactive.",
            )
        }

        val plate = LicensePlate.of(raw = input.plate)
        if (vehicles.existsByPlate(plate = plate)) {
            throw VehicleException.VehicleAlreadyExists(
                message = "Vehicle ${plate.formatted()} already exists.",
            )
        }

        val vehicle = Vehicle.register(
            ownerId = ownerId,
            plate = plate,
            brand = input.brand,
            model = input.model,
            color = input.color,
            year = ModelYear.of(year = input.year),
        )
        vehicles.save(vehicle = vehicle)
        return vehicle.toResponse()
    }
}
