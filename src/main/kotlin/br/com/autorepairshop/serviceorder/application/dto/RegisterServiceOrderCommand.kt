package br.com.autorepairshop.serviceorder.application.dto

data class RegisterServiceOrderCommand(
    val document: String,
    val vehiclePlate: String,
)
