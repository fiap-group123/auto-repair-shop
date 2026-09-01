package br.com.autorepairshop.catalog.application.event

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.event.ExtraServiceRegistered
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.mail.EmailSender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant

@Tag("unit")
class ExtraServiceRegisteredMailListenerTest {
    private val extras = mockk<ExtraServiceRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val customers = mockk<CustomerRepository>()
    private val emails = mockk<EmailSender>(relaxUnitFun = true)
    private val listener = ExtraServiceRegisteredMailListener(
        extras = extras,
        orders = orders,
        customers = customers,
        emails = emails,
    )

    @Test
    fun `sends an email when an extra is registered`() {
        val customer = CustomerFixtures.activeCustomer()
        val order = ServiceOrderFixtures.budgetApproved(customerId = customer.id.value)
        val extra = CatalogFixtures.extraService(serviceOrderId = order.id.value)
        every { extras.findById(id = extra.id) } returns extra
        every { orders.findById(id = order.id) } returns order
        every { customers.findById(id = customer.id) } returns customer

        listener.on(
            event = ExtraServiceRegistered(
                extraServiceId = extra.id,
                serviceOrderId = order.id.value,
                occurredOn = Instant.now(),
            ),
        )

        verify {
            emails.send(
                to = customer.contact.email.value,
                subject = "Reparo extra aguardando aprovacao",
                body = match { it.contains(other = extra.name.value) },
            )
        }
    }

    @Test
    fun `skips when the extra order or customer is missing or inactive`() {
        val customer = CustomerFixtures.inactiveCustomer()
        val order = ServiceOrderFixtures.budgetApproved(customerId = customer.id.value)
        val extra = CatalogFixtures.extraService(serviceOrderId = order.id.value)
        val event = ExtraServiceRegistered(
            extraServiceId = extra.id,
            serviceOrderId = order.id.value,
            occurredOn = Instant.now(),
        )
        every { extras.findById(id = extra.id) } returnsMany
            listOf(element = null)
                .plus(element = extra)
                .plus(element = extra)
                .plus(element = extra)
        every { orders.findById(id = order.id) } returnsMany
            listOf(element = null)
                .plus(element = order)
                .plus(element = order)
        every { customers.findById(id = customer.id) } returnsMany
            listOf(element = null).plus(element = customer)

        listener.on(event = event)
        listener.on(event = event)
        listener.on(event = event)
        listener.on(event = event)

        verify(exactly = 0) { emails.send(to = any(), subject = any(), body = any()) }
    }

    @Test
    fun `swallows send failures`() {
        val customer = CustomerFixtures.activeCustomer()
        val order = ServiceOrderFixtures.budgetApproved(customerId = customer.id.value)
        val extra = CatalogFixtures.extraService(serviceOrderId = order.id.value)
        every { extras.findById(id = extra.id) } returns extra
        every { orders.findById(id = order.id) } returns order
        every { customers.findById(id = customer.id) } returns customer
        every { emails.send(to = any(), subject = any(), body = any()) } throws IllegalStateException("smtp down")

        listener.on(
            event = ExtraServiceRegistered(
                extraServiceId = extra.id,
                serviceOrderId = order.id.value,
                occurredOn = Instant.now(),
            ),
        )
    }
}
