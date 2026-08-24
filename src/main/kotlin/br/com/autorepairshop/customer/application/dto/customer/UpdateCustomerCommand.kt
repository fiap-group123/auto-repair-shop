package br.com.autorepairshop.customer.application.dto.customer

import java.util.UUID

data class UpdateCustomerCommand(
    val customerId: UUID,
    val name: String,
    val email: String,
    val phone: String,
)
