package br.com.autorepairshop.serviceandexecution.application.dto

data class RegisterServiceOrderCommand(
    val document: String,
    val vehiclePlate: String,
)
