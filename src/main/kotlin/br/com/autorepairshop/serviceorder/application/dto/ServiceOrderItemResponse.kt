package br.com.autorepairshop.serviceorder.application.dto

import java.math.BigDecimal
import java.util.UUID

data class ServiceOrderItemResponse(
    val offeredServiceId: UUID,
    val description: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal,
)
