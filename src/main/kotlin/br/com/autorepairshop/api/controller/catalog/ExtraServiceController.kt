package br.com.autorepairshop.api.controller.catalog

import br.com.autorepairshop.api.dto.catalog.RegisterExtraServiceRequest
import br.com.autorepairshop.catalog.application.dto.ExtraServiceResponse
import br.com.autorepairshop.catalog.application.dto.RegisterExtraServiceCommand
import br.com.autorepairshop.catalog.application.usecase.ApproveExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FindExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.FinishExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.InProgressExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.ListExtraServicesByServiceOrderIdUseCase
import br.com.autorepairshop.catalog.application.usecase.RegisterExtraServiceUseCase
import br.com.autorepairshop.catalog.application.usecase.RejectExtraServiceUseCase
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
@RequestMapping("/extra-services")
@Tag(name = "ExtraService", description = "Additional repairs pending client approval (bounded context Catalog)")
class ExtraServiceController(
    private val registerExtra: RegisterExtraServiceUseCase,
    private val findExtra: FindExtraServiceUseCase,
    private val listByServiceOrder: ListExtraServicesByServiceOrderIdUseCase,
    private val approveExtra: ApproveExtraServiceUseCase,
    private val rejectExtra: RejectExtraServiceUseCase,
    private val inProgressExtra: InProgressExtraServiceUseCase,
    private val finishExtra: FinishExtraServiceUseCase,
) {

    @PostMapping
    @Operation(summary = "Register an extra service pending approval")
    fun register(@RequestBody request: RegisterExtraServiceRequest): ResponseEntity<ExtraServiceResponse> {
        val extra = registerExtra.execute(
            input = RegisterExtraServiceCommand(
                serviceOrderId = request.serviceOrderId,
                name = request.name,
                basePrice = request.basePrice,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(extra.id)
            .toUri()
        return ResponseEntity.created(location).body(extra)
    }

    @GetMapping("/service-order/{serviceOrderId}")
    @Operation(summary = "List extra services of a service order")
    fun listByServiceOrderId(@PathVariable serviceOrderId: UUID): ResponseEntity<List<ExtraServiceResponse>> =
        ResponseEntity.ok(listByServiceOrder.execute(input = serviceOrderId))

    @GetMapping("/{id}")
    @Operation(summary = "Find an extra service by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<ExtraServiceResponse> =
        ResponseEntity.ok(findExtra.execute(input = id))

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve an extra service and add it to the budget")
    fun approve(@PathVariable id: UUID): ResponseEntity<ExtraServiceResponse> =
        ResponseEntity.ok(approveExtra.execute(input = id))

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject an extra service")
    fun reject(@PathVariable id: UUID): ResponseEntity<ExtraServiceResponse> =
        ResponseEntity.ok(rejectExtra.execute(input = id))

    @PostMapping("/{id}/in-progress")
    @Operation(summary = "Start execution of an approved extra service")
    fun start(@PathVariable id: UUID): ResponseEntity<ExtraServiceResponse> =
        ResponseEntity.ok(inProgressExtra.execute(input = id))

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish an extra service in progress")
    fun finish(@PathVariable id: UUID): ResponseEntity<ExtraServiceResponse> =
        ResponseEntity.ok(finishExtra.execute(input = id))
}
