package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChangeVehiclePlateUseCase(
    private val vehicles: VehicleRepository,
) : UseCase<ChangeVehiclePlateCommand, VehicleResponse> {

    @Transactional
    override fun execute(input: ChangeVehiclePlateCommand): VehicleResponse {
        val vehicle = vehicles.findById(VehicleId(input.vehicleId))
            ?: throw VehicleException.VehicleNotFound("Vehicle ${input.vehicleId} was not found.")

        val newPlate = LicensePlate.of(input.plate)
        if (newPlate != vehicle.plate && vehicles.existsByPlate(newPlate)) {
            throw VehicleException.VehicleAlreadyExists(
                "Vehicle ${newPlate.formatted()} already exists."
            )
        }

        vehicle.changePlate(newPlate)
        vehicles.save(vehicle)
        return vehicle.toResponse()
    }
}
