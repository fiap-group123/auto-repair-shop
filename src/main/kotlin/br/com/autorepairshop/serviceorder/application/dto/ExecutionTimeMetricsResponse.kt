package br.com.autorepairshop.serviceorder.application.dto

data class ExecutionTimeMetricsResponse(
    val sampleSize: Int,
    val averageSeconds: Double,
)
