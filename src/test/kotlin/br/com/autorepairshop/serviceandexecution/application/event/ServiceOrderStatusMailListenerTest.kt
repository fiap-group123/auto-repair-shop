package br.com.autorepairshop.serviceandexecution.application.event

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceandexecution.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderBudgetRejected
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderExecutionStarted
import br.com.autorepairshop.serviceandexecution.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.mail.EmailSender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant

@Tag("unit")
class ServiceOrderStatusMailListenerTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val budgets = mockk<BudgetRepository>()
    private val customers = mockk<CustomerRepository>()
    private val emails = mockk<EmailSender>(relaxUnitFun = true)
    private val listener = ServiceOrderStatusMailListener(
        orders = orders,
        budgets = budgets,
        customers = customers,
        emails = emails,
    )

    @Test
    fun `sends an email for each service order status`() {
        val customer = CustomerFixtures.activeCustomer()
        val order = ServiceOrderFixtures.waitingApproval(customerId = customer.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { customers.findById(id = customer.id) } returns customer
        val occurredOn = Instant.now()

        listener.on(event = ServiceOrderOpened(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = DiagnosisStarted(serviceOrderId = order.id.value, occurredOn = occurredOn))
        listener.on(event = DiagnosisFinished(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = ServiceOrderApproved(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = ServiceOrderBudgetRejected(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = ServiceOrderExecutionStarted(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = ServiceOrderCompleted(serviceOrderId = order.id, occurredOn = occurredOn))
        listener.on(event = ServiceOrderDelivered(serviceOrderId = order.id, occurredOn = occurredOn))

        verify(exactly = 8) {
            emails.send(
                to = customer.contact.email.value,
                subject = any(),
                body = any(),
            )
        }
        verify {
            emails.send(
                to = customer.contact.email.value,
                subject = "Diagnostico iniciado",
                body = any(),
            )
            emails.send(
                to = customer.contact.email.value,
                subject = "Orcamento aprovado",
                body = any(),
            )
            emails.send(
                to = customer.contact.email.value,
                subject = "Orcamento rejeitado",
                body = any(),
            )
            emails.send(
                to = customer.contact.email.value,
                subject = "Ordem de servico em execucao",
                body = any(),
            )
            emails.send(
                to = customer.contact.email.value,
                subject = "Veiculo entregue",
                body = any(),
            )
            emails.send(
                to = customer.contact.email.value,
                subject = "Orcamento aguardando aprovacao",
                body = match { it.contains(other = "R$") },
            )
        }
    }

    @Test
    fun `skips when the order or customer is missing or inactive`() {
        val customer = CustomerFixtures.inactiveCustomer()
        val order = ServiceOrderFixtures.received(customerId = customer.id.value)
        every { orders.findById(id = order.id) } returnsMany
            listOf(element = null)
                .plus(element = order)
                .plus(element = order)
        every { customers.findById(id = customer.id) } returnsMany listOf(element = null).plus(element = customer)
        val event = ServiceOrderOpened(
            serviceOrderId = order.id,
            occurredOn = Instant.now(),
        )

        listener.on(event = event)
        listener.on(event = event)
        listener.on(event = event)

        verify(exactly = 0) { emails.send(to = any(), subject = any(), body = any()) }
    }

    @Test
    fun `swallows send failures`() {
        val customer = CustomerFixtures.activeCustomer()
        val order = ServiceOrderFixtures.received(customerId = customer.id.value)
        every { orders.findById(id = order.id) } returns order
        every { customers.findById(id = customer.id) } returns customer
        every { emails.send(to = any(), subject = any(), body = any()) } throws IllegalStateException("smtp down")

        listener.on(
            event = ServiceOrderOpened(
                serviceOrderId = order.id,
                occurredOn = Instant.now(),
            ),
        )
    }
}
