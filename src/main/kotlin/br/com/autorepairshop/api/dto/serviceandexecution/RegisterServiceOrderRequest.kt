package br.com.autorepairshop.api.dto.serviceorder

data class RegisterServiceOrderRequest(
    val document: String,
    val vehiclePlate: String,
)
