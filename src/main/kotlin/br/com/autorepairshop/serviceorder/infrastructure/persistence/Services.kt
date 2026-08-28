package br.com.autorepairshop.serviceorder.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.util.UUID

@Embeddable
class ServiceOrderItemColumn(
    @Column(name = "offered_service_id", nullable = false)
    var offeredServiceId: UUID,
    @Column(nullable = false, length = 60)
    var description: String,
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,
    @Column(nullable = false)
    var quantity: Int,
)
