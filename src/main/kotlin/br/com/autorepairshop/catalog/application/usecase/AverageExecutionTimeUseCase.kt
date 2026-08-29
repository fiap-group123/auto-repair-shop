package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.AverageExecutionTimeResponse
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AverageExecutionTimeUseCase(private val services: ServiceRepository) :
    UseCase<Unit, AverageExecutionTimeResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: Unit): AverageExecutionTimeResponse {
        val durations = services.findAll().mapNotNull { it.estimatedTime }
        if (durations.isEmpty()) {
            return AverageExecutionTimeResponse(
                sampleSize = 0,
                averageSeconds = null,
            )
        }
        val seconds = durations.map { it.inWholeSeconds }
        return AverageExecutionTimeResponse(
            sampleSize = seconds.size,
            averageSeconds = seconds.sum() / seconds.size,
        )
    }
}
