package br.com.autorepairshop.customer.application.usecase.vehicle

import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReactivateVehicleUseCase(private val vehicles: VehicleRepository): UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val vehicle = vehicles.findById(id = VehicleId(value = input))
            ?: throw VehicleException.VehicleNotFound(message = "Vehicle $input was not found.")
        vehicle.reactivate()
        vehicles.save(vehicle)
    }
}
