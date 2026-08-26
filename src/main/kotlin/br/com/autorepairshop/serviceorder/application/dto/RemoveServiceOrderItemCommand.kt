package br.com.autorepairshop.serviceorder.application.dto

import java.util.UUID

data class RemoveServiceOrderItemCommand(
    val serviceOrderId: UUID,
    val offeredServiceId: UUID,
)
