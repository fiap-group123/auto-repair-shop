package br.com.autorepairshop.api.controller.inputmanagment

import br.com.autorepairshop.api.dto.inputmanagment.AdjustInventoryStockRequest
import br.com.autorepairshop.api.dto.inputmanagment.RegisterInventoryRequest
import br.com.autorepairshop.api.dto.inputmanagment.UpdateInventoryRequest
import br.com.autorepairshop.inputmanagment.application.dto.AdjustInventoryStockCommand
import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.RegisterInventoryCommand
import br.com.autorepairshop.inputmanagment.application.dto.UpdateInventoryCommand
import br.com.autorepairshop.inputmanagment.application.usecase.AdjustInventoryStockUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.DeactivateInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.FindInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ListInventoriesUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.ReactivateInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterInventoryUseCase
import br.com.autorepairshop.inputmanagment.application.usecase.UpdateInventoryUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/inventories")
@Tag(name = "Inventory", description = "Workshop stock catalog (bounded context Inventory)")
class InventoryController(
    private val registerInventory: RegisterInventoryUseCase,
    private val listInventories: ListInventoriesUseCase,
    private val findInventory: FindInventoryUseCase,
    private val updateInventory: UpdateInventoryUseCase,
    private val deactivateInventory: DeactivateInventoryUseCase,
    private val reactivateInventory: ReactivateInventoryUseCase,
    private val adjustInventoryStock: AdjustInventoryStockUseCase,
) {

    @PostMapping
    @Operation(summary = "Register an inventory item")
    fun register(@RequestBody request: RegisterInventoryRequest): ResponseEntity<InventoryResponse> {
        val inventory = registerInventory.execute(
            input = RegisterInventoryCommand(
                name = request.name,
                kind = request.kind,
                unitPrice = request.unitPrice,
                stock = request.stock,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(inventory.id)
            .toUri()
        return ResponseEntity.created(location).body(inventory)
    }

    @GetMapping
    @Operation(summary = "List inventory items")
    fun list(): ResponseEntity<List<InventoryResponse>> = ResponseEntity.ok(listInventories.execute(input = Unit))

    @GetMapping("/{id}")
    @Operation(summary = "Find an inventory item by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<InventoryResponse> =
        ResponseEntity.ok(findInventory.execute(input = id))

    @PutMapping("/{id}")
    @Operation(summary = "Update name, unit price and/or kind")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateInventoryRequest,
    ): ResponseEntity<InventoryResponse> = ResponseEntity.ok(
        updateInventory.execute(
            input = UpdateInventoryCommand(
                inventoryId = id,
                name = request.name,
                unitPrice = request.unitPrice,
                kind = request.kind,
            ),
        ),
    )

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an inventory item")
    fun deactivate(@PathVariable id: UUID): ResponseEntity<InventoryResponse> =
        ResponseEntity.ok(deactivateInventory.execute(input = id))

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate an inventory item")
    fun reactivate(@PathVariable id: UUID): ResponseEntity<InventoryResponse> =
        ResponseEntity.ok(reactivateInventory.execute(input = id))

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Set the absolute stock quantity")
    fun setStock(
        @PathVariable id: UUID,
        @RequestBody request: AdjustInventoryStockRequest,
    ): ResponseEntity<InventoryResponse> = ResponseEntity.ok(
        adjustInventoryStock.execute(
            input = AdjustInventoryStockCommand(
                inventoryId = id,
                quantity = request.quantity,
            ),
        ),
    )
}
