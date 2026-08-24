package br.com.autorepairshop.customer.application.dto.customer

data class RegisterCustomerCommand(
    val documentId: String,
    val name: String,
    val email: String,
    val phone: String,
)
