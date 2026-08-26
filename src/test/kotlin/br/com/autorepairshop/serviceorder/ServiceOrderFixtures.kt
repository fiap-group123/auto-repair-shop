package br.com.autorepairshop.serviceorder

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderItem
import br.com.autorepairshop.shared.domain.Money
import java.math.BigDecimal
import java.util.UUID

object ServiceOrderFixtures {
    const val DESCRIPTION = "Troca de oleo"
    const val UNIT_PRICE = "150.00"

    fun item(
        offeredServiceId: UUID = UUID.randomUUID(),
        unitPrice: String = UNIT_PRICE,
        quantity: Int = 1,
    ): ServiceOrderItem = ServiceOrderItem(
        offeredServiceId = offeredServiceId,
        description = DESCRIPTION,
        unitPrice = Money.of(raw = BigDecimal(unitPrice)),
        quantity = quantity,
    )

    fun received(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = ServiceOrder.open(
        customerId = customerId,
        vehicleId = vehicleId,
    )

    /** In diagnosis without any requested service, so the budget is still empty. */
    fun inDiagnosisWithoutItems(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = received(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { startDiagnosis() }

    fun inDiagnosis(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inDiagnosisWithoutItems(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { addItem(item = item()) }

    fun waitingApproval(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inDiagnosis(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { finishDiagnosis() }

    fun inExecution(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = waitingApproval(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { approve() }

    fun completed(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inExecution(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { complete() }

    fun delivered(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = completed(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { deliver() }
}
