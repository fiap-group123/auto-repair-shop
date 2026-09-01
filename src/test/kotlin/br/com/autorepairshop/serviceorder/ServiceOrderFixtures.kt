package br.com.autorepairshop.serviceorder

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import java.util.UUID

object ServiceOrderFixtures {

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

    fun waitingApproval(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = inDiagnosis(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { finishDiagnosis() }

    fun budgetApproved(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = waitingApproval(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { budgetApprove() }

    fun budgetRejected(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = waitingApproval(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { budgetReject() }

    fun inExecution(
        customerId: UUID = UUID.randomUUID(),
        vehicleId: UUID = UUID.randomUUID(),
    ): ServiceOrder = budgetApproved(
        customerId = customerId,
        vehicleId = vehicleId,
    ).apply { startExecution() }

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
