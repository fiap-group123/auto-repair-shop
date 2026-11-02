package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.usecase.RegisterServiceUseCase
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.usecase.customer.RegisterCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.RegisterVehicleUseCase
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.customer.domain.valueobject.document.Document
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterPartUseCase
import br.com.autorepairshop.serviceandexecution.application.dto.CustomerOpening
import br.com.autorepairshop.serviceandexecution.application.dto.OpenServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceandexecution.application.dto.VehicleOpening
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OpenServiceOrderUseCase(
    private val customers: CustomerRepository,
    private val vehicles: VehicleRepository,
    private val registerCustomer: RegisterCustomerUseCase,
    private val registerVehicle: RegisterVehicleUseCase,
    private val registerOrder: RegisterServiceOrderUseCase,
    private val registerService: RegisterServiceUseCase,
    private val registerPart: RegisterPartUseCase,
    private val findOrder: FindServiceOrderUseCase,
) : UseCase<OpenServiceOrderCommand, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: OpenServiceOrderCommand): ServiceOrderResponse {
        val documentRaw = documentOf(input = input)
        val plateRaw = plateOf(input = input)
        val customerId = ensureCustomer(
            documentRaw = documentRaw,
            opening = input.customer,
        )
        ensureVehicle(
            plateRaw = plateRaw,
            customerId = customerId,
            opening = input.vehicle,
        )
        val order = registerOrder.execute(
            input = RegisterServiceOrderCommand(
                document = documentRaw,
                vehiclePlate = plateRaw,
            ),
        )
        attachLines(
            orderId = order.id,
            input = input,
        )
        return findOrder.execute(input = order.id)
    }

    private fun attachLines(
        orderId: UUID,
        input: OpenServiceOrderCommand,
    ) {
        input.services.forEach { line ->
            registerService.execute(
                input = RegisterServiceCommand(
                    serviceOrderId = orderId,
                    name = line.name,
                    basePrice = line.basePrice,
                ),
            )
        }
        input.parts.forEach { line ->
            registerPart.execute(
                input = RegisterPartCommand(
                    serviceOrderId = orderId,
                    inventoryId = line.inventoryId,
                    quantity = line.quantity,
                ),
            )
        }
    }

    private fun ensureCustomer(
        documentRaw: String,
        opening: CustomerOpening?,
    ): UUID {
        val document = Document.of(raw = documentRaw)
        val existing = customers.findByDocumentId(id = document)
        if (existing != null && !existing.active) {
            throw CustomerException.InvalidDocument(
                message = "Customer ${document.masked()} is inactive.",
            )
        }
        if (existing != null) {
            return existing.id.value
        }
        val payload = opening ?: throw ServiceOrderException.InvalidOpening(
            message = "Customer name, email and phone are required when the document is new.",
        )
        if (payload.name.isBlank() || payload.email.isBlank() || payload.phone.isBlank()) {
            throw ServiceOrderException.InvalidOpening(
                message = "Customer name, email and phone are required when the document is new.",
            )
        }
        return registerCustomer.execute(
            input = RegisterCustomerCommand(
                documentId = documentRaw,
                name = payload.name,
                email = payload.email,
                phone = payload.phone,
            ),
        ).id
    }

    private fun ensureVehicle(
        plateRaw: String,
        customerId: UUID,
        opening: VehicleOpening?,
    ) {
        val plate = LicensePlate.of(raw = plateRaw)
        val existing = vehicles.findByPlate(plate = plate)
        if (existing != null && existing.ownerId != CustomerId(value = customerId)) {
            throw ServiceOrderException.VehicleNotOwnedByCustomer(
                message = "Vehicle does not belong to this customer.",
            )
        }
        if (existing != null) {
            return
        }
        val payload = opening ?: throw ServiceOrderException.InvalidOpening(
            message = "Vehicle brand, model, color and year are required when the plate is new.",
        )
        registerVehicle.execute(
            input = RegisterVehicleCommand(
                ownerId = customerId,
                plate = plateRaw,
                brand = payload.brand,
                model = payload.model,
                color = payload.color,
                year = payload.year,
            ),
        )
    }

    private fun documentOf(input: OpenServiceOrderCommand): String {
        val raw = input.customer?.documentId?.takeIf { value -> value.isNotBlank() } ?: input.document
        if (raw.isNullOrBlank()) {
            throw ServiceOrderException.InvalidOpening(
                message = "Customer document is required.",
            )
        }
        return raw.trim()
    }

    private fun plateOf(input: OpenServiceOrderCommand): String {
        val raw = input.vehicle?.plate?.takeIf { value -> value.isNotBlank() } ?: input.vehiclePlate
        if (raw.isNullOrBlank()) {
            throw ServiceOrderException.InvalidOpening(
                message = "Vehicle plate is required.",
            )
        }
        return raw.trim()
    }
}
