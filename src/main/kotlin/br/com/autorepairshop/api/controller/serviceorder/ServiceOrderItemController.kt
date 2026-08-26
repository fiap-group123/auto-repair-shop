package br.com.autorepairshop.api.controller.serviceorder

import br.com.autorepairshop.api.dto.serviceorder.AddServiceOrderItemRequest
import br.com.autorepairshop.serviceorder.application.dto.AddServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.application.dto.RemoveServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.usecase.AddServiceOrderItemUseCase
import br.com.autorepairshop.serviceorder.application.usecase.RemoveServiceOrderItemUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/service-orders/{serviceOrderId}/items")
@Tag(name = "ServiceOrder", description = "Requested services that make up the service order budget")
class ServiceOrderItemController(
    private val addItem: AddServiceOrderItemUseCase,
    private val removeItem: RemoveServiceOrderItemUseCase,
) {

    @PostMapping
    @Operation(summary = "Add a requested service to the budget")
    fun add(
        @PathVariable serviceOrderId: UUID,
        @RequestBody request: AddServiceOrderItemRequest,
    ): ResponseEntity<ServiceOrderResponse> = ResponseEntity.ok(
        addItem.execute(
            input = AddServiceOrderItemCommand(
                serviceOrderId = serviceOrderId,
                offeredServiceId = request.offeredServiceId,
                quantity = request.quantity,
            ),
        ),
    )

    @DeleteMapping("/{offeredServiceId}")
    @Operation(summary = "Remove a requested service from the budget")
    fun remove(
        @PathVariable serviceOrderId: UUID,
        @PathVariable offeredServiceId: UUID,
    ): ResponseEntity<ServiceOrderResponse> = ResponseEntity.ok(
        removeItem.execute(
            input = RemoveServiceOrderItemCommand(
                serviceOrderId = serviceOrderId,
                offeredServiceId = offeredServiceId,
            ),
        ),
    )
}
