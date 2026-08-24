package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindVehicleByPlateUseCase(
    private val vehicles: VehicleRepository,
) : UseCase<String, VehicleResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: String): VehicleResponse {
        val plate = LicensePlate.of(input)
        val vehicle = vehicles.findByPlate(plate)
            ?: throw VehicleException.VehicleNotFound(
                "Vehicle ${plate.formatted()} was not found."
            )
        return vehicle.toResponse()
    }
}
