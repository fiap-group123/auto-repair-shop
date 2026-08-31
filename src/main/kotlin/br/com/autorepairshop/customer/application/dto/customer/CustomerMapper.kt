package br.com.autorepairshop.customer.application.dto.customer

import br.com.autorepairshop.customer.domain.aggregate.Customer
import kotlin.time.toJavaInstant

fun Customer.toResponse() = CustomerResponse(
    id = id.value,
    documentId = document.formatted(),
    documentType = document.type.name,
    name = name.value,
    email = contact.email.value,
    phone = contact.phone.value,
    active = active,
    createdAt = createdAt.toJavaInstant(),
)
