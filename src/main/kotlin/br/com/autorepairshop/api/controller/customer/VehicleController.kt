package br.com.autorepairshop.api.controller.customer

import br.com.autorepairshop.api.dto.customer.ChangeVehiclePlateRequest
import br.com.autorepairshop.api.dto.customer.RegisterVehicleRequest
import br.com.autorepairshop.api.dto.customer.TransferVehicleRequest
import br.com.autorepairshop.api.dto.customer.UpdateVehicleSpecRequest
import br.com.autorepairshop.api.security.AuthorizationSupport
import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.TransferVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.UpdateVehicleSpecCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.usecase.vehicle.ChangeVehiclePlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleByPlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.ListVehiclesByOwnerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.RegisterVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.TransferVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.UpdateVehicleSpecUseCase
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle", description = "Vehicle search and update")
class VehicleController(
    private val registerVehicle: RegisterVehicleUseCase,
    private val findVehicle: FindVehicleUseCase,
    private val findVehicleByPlate: FindVehicleByPlateUseCase,
    private val listVehiclesByOwner: ListVehiclesByOwnerUseCase,
    private val updateVehicleSpec: UpdateVehicleSpecUseCase,
    private val changeVehiclePlate: ChangeVehiclePlateUseCase,
    private val transferVehicle: TransferVehicleUseCase,
    private val authorization: AuthorizationSupport,
) {

    @PostMapping
    @Operation(summary = "Register customer vehicle")
    fun register(@RequestBody request: RegisterVehicleRequest): ResponseEntity<VehicleResponse> {
        val vehicle = registerVehicle.execute(
            input = RegisterVehicleCommand(
                ownerId = request.ownerId,
                plate = request.plate,
                brand = request.brand,
                model = request.model,
                year = request.year,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(vehicle.id)
            .toUri()
        return ResponseEntity.created(location).body(vehicle)
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "List vehicles owned by a customer")
    fun listByOwner(@PathVariable ownerId: UUID): ResponseEntity<List<VehicleResponse>> {
        authorization.requireCanAccessCustomer(customerId = ownerId)
        return ResponseEntity.ok(listVehiclesByOwner.execute(input = ownerId))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search for vehicle by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<VehicleResponse> {
        val vehicle = findVehicle.execute(input = id)
        authorization.requireCanAccessVehicleOwner(ownerId = vehicle.ownerId)
        return ResponseEntity.ok(vehicle)
    }

    @GetMapping
    @Operation(summary = "Search for vehicle by license plate")
    fun findByPlate(@RequestParam plate: String): ResponseEntity<VehicleResponse> {
        val vehicle = findVehicleByPlate.execute(input = plate)
        authorization.requireCanAccessVehicleOwner(ownerId = vehicle.ownerId)
        return ResponseEntity.ok(vehicle)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand, model and/or year")
    fun updateSpec(
        @PathVariable id: UUID,
        @RequestBody request: UpdateVehicleSpecRequest,
    ): ResponseEntity<VehicleResponse> = ResponseEntity.ok(
        updateVehicleSpec.execute(
            input = UpdateVehicleSpecCommand(
                vehicleId = id,
                brand = request.brand,
                model = request.model,
                year = request.year,
            ),
        ),
    )

    @PatchMapping("/{id}/plate")
    @Operation(summary = "Change license plate")
    fun changePlate(
        @PathVariable id: UUID,
        @RequestBody request: ChangeVehiclePlateRequest,
    ): ResponseEntity<VehicleResponse> = ResponseEntity.ok(
        changeVehiclePlate.execute(
            input = ChangeVehiclePlateCommand(
                vehicleId = id,
                plate = request.plate,
            ),
        ),
    )

    @PatchMapping("/{id}/owner")
    @Operation(summary = "Transfer vehicle to another customer")
    fun transfer(
        @PathVariable id: UUID,
        @RequestBody request: TransferVehicleRequest,
    ): ResponseEntity<VehicleResponse> = ResponseEntity.ok(
        transferVehicle.execute(
            input = TransferVehicleCommand(
                vehicleId = id,
                newOwnerId = request.newOwnerId,
            ),
        ),
    )
}
