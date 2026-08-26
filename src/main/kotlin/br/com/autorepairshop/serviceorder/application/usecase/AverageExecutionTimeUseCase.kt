package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.ExecutionTimeMetricsResponse
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AverageExecutionTimeUseCase(private val orders: ServiceOrderRepository) :
    UseCase<Unit, ExecutionTimeMetricsResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): ExecutionTimeMetricsResponse {
        val durations = orders.findExecuted().mapNotNull { it.executionDuration() }
        if (durations.isEmpty()) {
            return ExecutionTimeMetricsResponse(
                sampleSize = 0,
                averageSeconds = 0.0,
            )
        }
        return ExecutionTimeMetricsResponse(
            sampleSize = durations.size,
            averageSeconds = durations.sumOf { it.inWholeSeconds }.toDouble() / durations.size,
        )
    }
}
