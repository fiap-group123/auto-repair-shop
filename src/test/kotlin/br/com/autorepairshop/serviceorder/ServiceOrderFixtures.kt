package br.com.autorepairshop.serviceorder

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.shared.domain.Money
import java.math.BigDecimal
import java.util.UUID

object ServiceOrderFixtures {
    val TOTAL: Money = Money.of(raw = BigDecimal("150.00"))

    fun received(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = ServiceOrder.open(
        customerId = customerId,
        vehicleId = vehicleId,
    )

    fun inDiagnosis(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = received(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { startDiagnosis() }

    /** In diagnosis with a budget already priced, so it can be sent for approval. */
    fun inDiagnosisWithBudget(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inDiagnosis(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { updateBudgetTotal(total = TOTAL) }

    fun waitingApproval(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inDiagnosisWithBudget(
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
    ).apply { finish() }

    fun delivered(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = completed(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { deliver() }
}
