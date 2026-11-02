package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class GetServiceOrderStatusUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = GetServiceOrderStatusUseCase(
        orders = orders,
        access = access,
    )

    @Test
    fun `returns the public label of every status`() {
        val samples = buildList {
            add(element = ServiceOrderFixtures.received() to "Recebida")
            add(element = ServiceOrderFixtures.inDiagnosis() to "Diagnóstico")
            add(element = ServiceOrderFixtures.waitingApproval() to "Aguardando Aprovação")
            add(element = ServiceOrderFixtures.budgetApproved() to "Orçamento aprovado")
            add(element = ServiceOrderFixtures.inExecution() to "Execução")
            add(element = ServiceOrderFixtures.completed() to "Finalizada")
            add(element = ServiceOrderFixtures.delivered() to "Entregue")
            add(element = ServiceOrderFixtures.budgetRejected() to "Orçamento recusado")
        }

        samples.forEach { (order, label) ->
            every { orders.findById(id = order.id) } returns order
            val response = useCase.execute(input = order.id.value)
            assertEquals(expected = order.status.name, actual = response.status)
            assertEquals(expected = label, actual = response.label)
            verify { access.requireCustomer(customerId = order.customerId) }
        }
    }

    @Test
    fun `throws when the order is missing`() {
        val missing = ServiceOrderFixtures.received()
        every { orders.findById(id = missing.id) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = missing.id.value)
        }
    }

    @Test
    fun `finished orders keep the finished label`() {
        val order = ServiceOrderFixtures.completed()
        every { orders.findById(id = order.id) } returns order

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = ServiceOrderStatus.FINISHED.name,
            actual = response.status,
        )
    }
}
