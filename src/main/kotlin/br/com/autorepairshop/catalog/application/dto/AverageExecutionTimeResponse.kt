package br.com.autorepairshop.catalog.application.dto

data class AverageExecutionTimeResponse(
    val sampleSize: Int,
    val averageSeconds: Long?,
)
