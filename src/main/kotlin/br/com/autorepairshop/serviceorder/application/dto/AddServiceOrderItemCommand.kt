package br.com.autorepairshop.serviceorder.application.dto

import java.util.UUID

data class AddServiceOrderItemCommand(
    val serviceOrderId: UUID,
    val offeredServiceId: UUID,
    val quantity: Int,
)
