package br.com.autorepairshop.serviceorder.application.dto

import java.util.UUID

data class OpenServiceOrderCommand(
    val customerId: UUID,
    val vehicleId: UUID,
)
