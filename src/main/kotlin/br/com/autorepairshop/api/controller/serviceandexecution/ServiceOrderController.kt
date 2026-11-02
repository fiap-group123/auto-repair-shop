package br.com.autorepairshop.api.controller.serviceandexecution

import br.com.autorepairshop.api.dto.serviceandexecution.RegisterServiceOrderRequest
import br.com.autorepairshop.serviceandexecution.application.dto.CustomerOpening
import br.com.autorepairshop.serviceandexecution.application.dto.OpenServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.PartOpening
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOpening
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderStatusResponse
import br.com.autorepairshop.serviceandexecution.application.dto.VehicleOpening
import br.com.autorepairshop.serviceandexecution.application.usecase.DeliverServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FindServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FinishDiagnosisUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FinishServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.GetServiceOrderStatusUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.ListServiceOrdersByCustomerIdUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.ListServiceOrdersUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.OpenServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.StartDiagnosisUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.StartExecutionUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/service-orders")
@Tag(name = "ServiceOrder", description = "Service order lifecycle (bounded context Service Order)")
class ServiceOrderController(
    private val openOrder: OpenServiceOrderUseCase,
    private val findOrder: FindServiceOrderUseCase,
    private val orderStatus: GetServiceOrderStatusUseCase,
    private val listOrders: ListServiceOrdersUseCase,
    private val listOrdersByCustomerId: ListServiceOrdersByCustomerIdUseCase,
    private val startDiagnosis: StartDiagnosisUseCase,
    private val finishDiagnosis: FinishDiagnosisUseCase,
    private val startExecution: StartExecutionUseCase,
    private val completeOrder: FinishServiceOrderUseCase,
    private val deliverOrder: DeliverServiceOrderUseCase,
) {

    @PostMapping
    @Operation(summary = "Open a service order with customer, vehicle, services and parts")
    fun register(@RequestBody request: RegisterServiceOrderRequest): ResponseEntity<ServiceOrderResponse> {
        val order = openOrder.execute(input = request.toCommand())
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.id)
            .toUri()
        return ResponseEntity.created(location).body(order)
    }

    @GetMapping
    @Operation(summary = "List the work queue, oldest first, without finished or delivered orders")
    fun list(): ResponseEntity<List<ServiceOrderResponse>> = ResponseEntity.ok(listOrders.execute(input = Unit))

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List service orders by customer id")
    fun listByCustomerId(@PathVariable customerId: UUID): ResponseEntity<List<ServiceOrderResponse>> =
        ResponseEntity.ok(listOrdersByCustomerId.execute(input = customerId))

    @GetMapping("/{id}/status")
    @Operation(summary = "Read the current service order status")
    fun status(@PathVariable id: UUID): ResponseEntity<ServiceOrderStatusResponse> =
        ResponseEntity.ok(orderStatus.execute(input = id))

    @GetMapping("/{id}")
    @Operation(summary = "Get a service order by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(findOrder.execute(input = id))

    @PostMapping("/{id}/diagnosis")
    @Operation(summary = "Start diagnosis")
    fun startDiagnosis(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(startDiagnosis.execute(input = id))

    @PostMapping("/{id}/diagnosis/finish")
    @Operation(summary = "Finish diagnosis and wait for approval")
    fun finishDiagnosis(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(finishDiagnosis.execute(input = id))

    @PostMapping("/{id}/execution")
    @Operation(summary = "Start execution after budget approval")
    fun startExecution(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(startExecution.execute(input = id))

    @PostMapping("/{id}/finish")
    @Operation(summary = "Complete execution")
    fun complete(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(completeOrder.execute(input = id))

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Deliver the vehicle")
    fun deliver(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(deliverOrder.execute(input = id))
}

private fun RegisterServiceOrderRequest.toCommand() = OpenServiceOrderCommand(
    document = document,
    vehiclePlate = vehiclePlate,
    customer = customer?.let { opening ->
        CustomerOpening(
            documentId = opening.documentId,
            name = opening.name,
            email = opening.email,
            phone = opening.phone,
        )
    },
    vehicle = vehicle?.let { opening ->
        VehicleOpening(
            plate = opening.plate,
            brand = opening.brand,
            model = opening.model,
            year = opening.year,
            color = opening.color,
        )
    },
    services = services.map { line ->
        ServiceOpening(
            name = line.name,
            basePrice = line.basePrice,
        )
    },
    parts = parts.map { line ->
        PartOpening(
            inventoryId = line.inventoryId,
            quantity = line.quantity,
        )
    },
)
