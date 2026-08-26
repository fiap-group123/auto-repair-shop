package br.com.autorepairshop.api.controller.serviceorder

import br.com.autorepairshop.serviceorder.application.dto.ExecutionTimeMetricsResponse
import br.com.autorepairshop.serviceorder.application.usecase.AverageExecutionTimeUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/service-orders/metrics")
@Tag(name = "ServiceOrder", description = "Service order execution metrics")
class ServiceOrderMetricsController(private val averageExecutionTime: AverageExecutionTimeUseCase) {

    @GetMapping("/execution-time")
    @Operation(summary = "Average execution time between budget approval and completion")
    fun executionTime(): ResponseEntity<ExecutionTimeMetricsResponse> =
        ResponseEntity.ok(averageExecutionTime.execute(input = Unit))
}
