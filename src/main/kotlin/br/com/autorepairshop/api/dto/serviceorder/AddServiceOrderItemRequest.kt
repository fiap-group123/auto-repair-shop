package br.com.autorepairshop.api.dto.serviceorder

import java.util.UUID

data class AddServiceOrderItemRequest(
    val offeredServiceId: UUID,
    val quantity: Int = 1,
)
