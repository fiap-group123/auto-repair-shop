package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindVehicleUseCase(
    private val vehicles: VehicleRepository,
) : UseCase<UUID, VehicleResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): VehicleResponse {
        val vehicle = vehicles.findById(VehicleId(input))
            ?: throw VehicleException.VehicleNotFound("Vehicle $input was not found.")
        return vehicle.toResponse()
    }
}
