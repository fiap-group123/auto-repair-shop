package br.com.autorepairshop.api.dto.serviceandexecution

data class RegisterServiceOrderRequest(
    val document: String,
    val vehiclePlate: String,
)
