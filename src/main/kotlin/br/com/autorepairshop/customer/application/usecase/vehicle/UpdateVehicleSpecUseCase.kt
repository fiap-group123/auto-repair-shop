package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.UpdateVehicleSpecCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateVehicleSpecUseCase(
    private val vehicles: VehicleRepository,
) : UseCase<UpdateVehicleSpecCommand, VehicleResponse> {

    @Transactional
    override fun execute(input: UpdateVehicleSpecCommand): VehicleResponse {
        val vehicle = vehicles.findById(VehicleId(input.vehicleId))
            ?: throw VehicleException.VehicleNotFound("Vehicle ${input.vehicleId} was not found.")

        vehicle.updateSpec(
            brand = input.brand,
            model = input.model,
            year = input.year?.let { ModelYear.of(year = it) },
        )
        vehicles.save(vehicle = vehicle)
        return vehicle.toResponse()
    }
}
