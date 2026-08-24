package br.com.autorepairshop.customer.application.dto.customer

import java.util.UUID

data class UpdateCustomerCommand(
    val customerId: UUID,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
