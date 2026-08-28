package br.com.autorepairshop.api.controller.serviceorder

import br.com.autorepairshop.api.dto.serviceorder.OpenServiceOrderRequest
import br.com.autorepairshop.api.security.AuthorizationSupport
import br.com.autorepairshop.api.security.CurrentUser
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.serviceorder.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.usecase.ApproveServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.DeliverServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FindServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FinishDiagnosisUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FinishServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.ListServiceOrdersByCustomerIdUseCase
import br.com.autorepairshop.serviceorder.application.usecase.ListServiceOrdersUseCase
import br.com.autorepairshop.serviceorder.application.usecase.RegisterServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.StartDiagnosisUseCase
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
    private val openOrder: RegisterServiceOrderUseCase,
    private val findOrder: FindServiceOrderUseCase,
    private val listOrders: ListServiceOrdersUseCase,
    private val listOrdersByCustomerId: ListServiceOrdersByCustomerIdUseCase,
    private val startDiagnosisUseCase: StartDiagnosisUseCase,
    private val finishDiagnosisUseCase: FinishDiagnosisUseCase,
    private val approveOrder: ApproveServiceOrderUseCase,
    private val completeOrder: FinishServiceOrderUseCase,
    private val deliverOrder: DeliverServiceOrderUseCase,
    private val authorization: AuthorizationSupport,
    private val currentUser: CurrentUser,
) {

    @PostMapping
    @Operation(summary = "Open a service order")
    fun open(@RequestBody request: OpenServiceOrderRequest): ResponseEntity<ServiceOrderResponse> {
        val order = openOrder.execute(
            input = RegisterServiceOrderCommand(
                customerId = request.customerId,
                vehicleId = request.vehicleId,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.id)
            .toUri()
        return ResponseEntity.created(location).body(order)
    }

    @GetMapping
    @Operation(summary = "List service orders")
    fun list(): ResponseEntity<List<ServiceOrderResponse>> = ResponseEntity.ok(listOrders.execute(input = Unit))

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List service orders by customer id")
    fun listByCustomerId(@PathVariable customerId: UUID): ResponseEntity<List<ServiceOrderResponse>> {
        val user = currentUser.get()
        if (user.role != Role.CLIENT) {
            throw AuthenticationException.Forbidden(message = "Only clients can list service orders by customer id.")
        }
        return ResponseEntity.ok(listOrdersByCustomerId.execute(input = customerId))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a service order by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> {
        val order = findOrder.execute(input = id)
        authorization.requireCanAccessServiceOrder(customerId = order.customerId)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{id}/diagnosis")
    @Operation(summary = "Start diagnosis")
    fun startDiagnosis(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(startDiagnosisUseCase.execute(input = id))

    @PostMapping("/{id}/diagnosis/complete")
    @Operation(summary = "Finish diagnosis and wait for approval")
    fun finishDiagnosis(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(finishDiagnosisUseCase.execute(input = id))

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve service order")
    fun approve(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> {
        val order = findOrder.execute(input = id)
        authorization.requireCanAccessServiceOrder(customerId = order.customerId)
        return ResponseEntity.ok(approveOrder.execute(input = id))
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete execution")
    fun complete(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(completeOrder.execute(input = id))

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Deliver the vehicle")
    fun deliver(@PathVariable id: UUID): ResponseEntity<ServiceOrderResponse> =
        ResponseEntity.ok(deliverOrder.execute(input = id))
}
