package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.authentication.application.security.AccessGuard
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
    private val access: AccessGuard,
) : UseCase<String, VehicleResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: String): VehicleResponse {
        val plate = LicensePlate.of(raw = input)
        val vehicle = vehicles.findByPlate(plate = plate)
            ?: throw VehicleException.VehicleNotFound(
                message = "Vehicle ${plate.formatted()} was not found.",
            )
        access.requireCustomer(customerId = vehicle.ownerId.value)
        return vehicle.toResponse()
    }
}
