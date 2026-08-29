package br.com.autorepairshop.api.controller.catalog

import br.com.autorepairshop.api.dto.catalog.RegisterServiceRequest
import br.com.autorepairshop.api.dto.catalog.UpdateServiceRequest
import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.UpdateServiceCommand
import br.com.autorepairshop.catalog.application.usecase.FindServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FinishServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.InProgressServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.ListServicesUseCase
import br.com.autorepairshop.catalog.application.usecase.RegisterServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.UpdateServiceUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
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
@RequestMapping("/services")
@Tag(name = "Catalog", description = "Services requested in a service order (bounded context Catalog)")
class ServiceController(
    private val registerService: RegisterServiceUseCase,
    private val updateService: UpdateServiceUseCase,
    private val inProgressService: InProgressServiceUseCase,
    private val finishService: FinishServiceUseCase,
    private val findService: FindServiceUseCase,
    private val listServices: ListServicesUseCase,
) {

    @PostMapping
    @Operation(summary = "Register service")
    fun register(@RequestBody request: RegisterServiceRequest): ResponseEntity<ServiceResponse> {
        val service = registerService.execute(
            input = RegisterServiceCommand(
                serviceOrderId = request.serviceOrderId,
                name = request.name,
                basePrice = request.basePrice,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(service.id)
            .toUri()
        return ResponseEntity.created(location).body(service)
    }

    @GetMapping
    @Operation(summary = "List services")
    fun list(): ResponseEntity<List<ServiceResponse>> = ResponseEntity.ok(listServices.execute(input = Unit))

    @GetMapping("/{id}")
    @Operation(summary = "Search for an offered service by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<ServiceResponse> =
        ResponseEntity.ok(findService.execute(input = id))

    @PostMapping("/{id}/in-progress")
    @Operation(summary = "Start executing the service")
    fun start(@PathVariable id: UUID): ResponseEntity<ServiceResponse> =
        ResponseEntity.ok(inProgressService.execute(input = id))

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish the service and record how long it took")
    fun finish(@PathVariable id: UUID): ResponseEntity<ServiceResponse> =
        ResponseEntity.ok(finishService.execute(input = id))

    @PutMapping("/{id}")
    @Operation(summary = "Update name and/or price")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateServiceRequest,
    ): ResponseEntity<ServiceResponse> = ResponseEntity.ok(
        updateService.execute(
            input = UpdateServiceCommand(
                serviceId = id,
                name = request.name,
                basePrice = request.price,
            ),
        ),
    )
}
