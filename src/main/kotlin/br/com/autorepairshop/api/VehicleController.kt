package br.com.autorepairshop.api

import br.com.autorepairshop.api.dto.ChangeVehiclePlateRequest
import br.com.autorepairshop.api.dto.TransferVehicleRequest
import br.com.autorepairshop.api.dto.UpdateVehicleSpecRequest
import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.application.dto.vehicle.TransferVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.UpdateVehicleSpecCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.usecase.vehicle.ChangeVehiclePlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleByPlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.TransferVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.UpdateVehicleSpecUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle", description = "Consulta e alteração de veículos")
class VehicleController(
    private val findVehicle: FindVehicleUseCase,
    private val findVehicleByPlate: FindVehicleByPlateUseCase,
    private val updateVehicleSpec: UpdateVehicleSpecUseCase,
    private val changeVehiclePlate: ChangeVehiclePlateUseCase,
    private val transferVehicle: TransferVehicleUseCase,
) {

    @GetMapping("/{id}")
    @Operation(summary = "Search for vehicle by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<VehicleResponse> =
        ResponseEntity.ok(findVehicle.execute(input = id))

    @GetMapping
    @Operation(summary = "Search for vehicle by license plate")
    fun findByPlate(@RequestParam plate: String): ResponseEntity<VehicleResponse> =
        ResponseEntity.ok(findVehicleByPlate.execute(input = plate))

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
