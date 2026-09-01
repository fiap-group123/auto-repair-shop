package br.com.autorepairshop.api.controller.inputmanagment

import br.com.autorepairshop.api.dto.inputmanagment.RegisterPartRequest
import br.com.autorepairshop.api.dto.inputmanagment.UpdatePartRequest
import br.com.autorepairshop.inputmanagment.application.dto.PartResponse
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.application.dto.UpdatePartCommand
import br.com.autorepairshop.inputmanagment.application.usecase.DeletePartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.FindPartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ListPartsByServiceOrderIdUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterPartUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.UpdatePartUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/parts")
@Tag(name = "Part", description = "Parts and supplies on a service order (bounded context Inventory)")
class PartController(
    private val registerPart: RegisterPartUseCase,
    private val findPart: FindPartUseCase,
    private val listByServiceOrder: ListPartsByServiceOrderIdUseCase,
    private val updatePart: UpdatePartUseCase,
    private val deletePart: DeletePartUseCase,
) {

    @PostMapping
    @Operation(summary = "Add a catalog item to a service order and decrement stock")
    fun register(@RequestBody request: RegisterPartRequest): ResponseEntity<PartResponse> {
        val part = registerPart.execute(
            input = RegisterPartCommand(
                serviceOrderId = request.serviceOrderId,
                inventoryId = request.inventoryId,
                quantity = request.quantity,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(part.id)
            .toUri()
        return ResponseEntity.created(location).body(part)
    }

    @GetMapping("/service-order/{serviceOrderId}")
    @Operation(summary = "List parts of a service order")
    fun listByServiceOrderId(@PathVariable serviceOrderId: UUID): ResponseEntity<List<PartResponse>> =
        ResponseEntity.ok(listByServiceOrder.execute(input = serviceOrderId))

    @GetMapping("/{id}")
    @Operation(summary = "Find a part by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<PartResponse> =
        ResponseEntity.ok(findPart.execute(input = id))

    @PutMapping("/{id}")
    @Operation(summary = "Change the quantity of a part on the order")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdatePartRequest,
    ): ResponseEntity<PartResponse> = ResponseEntity.ok(
        updatePart.execute(
            input = UpdatePartCommand(
                partId = id,
                quantity = request.quantity,
            ),
        ),
    )

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a part from the order and restore stock")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        deletePart.execute(input = id)
        return ResponseEntity.noContent().build()
    }
}
