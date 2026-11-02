package br.com.autorepairshop.serviceandexecution.application.dto

import java.util.UUID

data class ServiceOrderStatusResponse(
    val id: UUID,
    val status: String,
    val label: String,
)
