package br.com.autorepairshop.api.controller.catalog

import br.com.autorepairshop.api.dto.catalog.RegisterOfferedServiceRequest
import br.com.autorepairshop.api.dto.catalog.UpdateOfferedServiceRequest
import br.com.autorepairshop.catalog.application.dto.OfferedServiceResponse
import br.com.autorepairshop.catalog.application.dto.RegisterOfferedServiceCommand
import br.com.autorepairshop.catalog.application.dto.UpdateOfferedServiceCommand
import br.com.autorepairshop.catalog.application.usecase.DeactivateOfferedServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FindOfferedServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.ListOfferedServicesUseCase
import br.com.autorepairshop.catalog.application.usecase.ReactivateOfferedServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.RegisterOfferedServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.UpdateOfferedServiceUseCase
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
@RequestMapping("/services")
@Tag(name = "Catalog", description = "Offered services catalog (bounded context Catalog)")
class OfferedServiceController(
    private val registerService: RegisterOfferedServiceUseCase,
    private val updateService: UpdateOfferedServiceUseCase,
    private val deactivateService: DeactivateOfferedServiceUseCase,
    private val reactivateService: ReactivateOfferedServiceUseCase,
    private val findService: FindOfferedServiceUseCase,
    private val listServices: ListOfferedServicesUseCase,
) {

    @PostMapping
    @Operation(summary = "Register offered service")
    fun register(@RequestBody request: RegisterOfferedServiceRequest): ResponseEntity<OfferedServiceResponse> {
        val service = registerService.execute(
            input = RegisterOfferedServiceCommand(
                name = request.name,
                price = request.price,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(service.id)
            .toUri()
        return ResponseEntity.created(location).body(service)
    }

    @GetMapping
    @Operation(summary = "List offered services")
    fun list(): ResponseEntity<List<OfferedServiceResponse>> = ResponseEntity.ok(listServices.execute(input = Unit))

    @GetMapping("/{id}")
    @Operation(summary = "Search for an offered service by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<OfferedServiceResponse> =
        ResponseEntity.ok(findService.execute(input = id))

    @PutMapping("/{id}")
    @Operation(summary = "Update name and/or price")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateOfferedServiceRequest,
    ): ResponseEntity<OfferedServiceResponse> = ResponseEntity.ok(
        updateService.execute(
            input = UpdateOfferedServiceCommand(
                serviceId = id,
                name = request.name,
                price = request.price,
            ),
        ),
    )

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate offered service (keeps service order history)")
    fun deactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        deactivateService.execute(input = id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}")
    @Operation(summary = "Reactivate offered service")
    fun reactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        reactivateService.execute(input = id)
        return ResponseEntity.noContent().build()
    }
}
