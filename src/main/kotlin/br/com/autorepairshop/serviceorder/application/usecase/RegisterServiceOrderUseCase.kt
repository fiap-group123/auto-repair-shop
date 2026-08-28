package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.VehicleId
import br.com.autorepairshop.serviceorder.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterServiceOrderUseCase(
    private val customers: CustomerRepository,
    private val vehicles: VehicleRepository,
    private val orders: ServiceOrderRepository,
    private val events: EventPublisher,
) : UseCase<RegisterServiceOrderCommand, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: RegisterServiceOrderCommand): ServiceOrderResponse {
        val customer = customers.findById(id = CustomerId(value = input.customerId))
            ?: throw CustomerException.CustomerNotFound(
                message = "Customer ${input.customerId} was not found.",
            )
        if (!customer.active) {
            throw CustomerException.InvalidDocument(
                message = "Customer ${customer.document.masked()} is inactive.",
            )
        }
        val vehicle = vehicles.findById(id = VehicleId(value = input.vehicleId))
            ?: throw VehicleException.VehicleNotFound(
                message = "Vehicle ${input.vehicleId} was not found.",
            )
        if (vehicle.ownerId != customer.id) {
            throw ServiceOrderException.VehicleNotOwnedByCustomer(
                message = "Vehicle does not belong to this customer.",
            )
        }
        if (orders.existsOpenByVehicleId(vehicleId = input.vehicleId)) {
            throw ServiceOrderException.OpenOrderAlreadyExists(
                message = "Vehicle already has an open service order.",
            )
        }
        val order = ServiceOrder.open(
            customerId = input.customerId,
            vehicleId = input.vehicleId,
        )
        orders.save(order = order)
        events.publish(aggregate = order)
        return order.toResponse()
    }
}
