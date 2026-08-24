package br.com.autorepairshop.customer.application.dto.customer

import br.com.autorepairshop.customer.domain.aggregate.Customer

fun Customer.toResponse() = CustomerResponse(
    id = id.value,
    documentId = documentId.formatted(),
    documentType = documentId.type.name,
    name = name.value,
    email = contact.email.value,
    phone = contact.phone.value,
    active = active,
)
