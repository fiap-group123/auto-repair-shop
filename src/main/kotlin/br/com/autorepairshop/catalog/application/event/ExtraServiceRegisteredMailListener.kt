package br.com.autorepairshop.catalog.application.event

import br.com.autorepairshop.catalog.domain.event.ExtraServiceRegistered
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.mail.EmailSender
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ExtraServiceRegisteredMailListener(
    private val extras: ExtraServiceRepository,
    private val orders: ServiceOrderRepository,
    private val customers: CustomerRepository,
    private val emails: EmailSender,
) {
    private val log = LoggerFactory.getLogger(ExtraServiceRegisteredMailListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ExtraServiceRegistered) {
        val extra = extras.findById(id = event.extraServiceId) ?: return
        val order = orders.findById(id = ServiceOrderId(value = extra.serviceOrderId)) ?: return
        val customer = customers.findById(id = CustomerId(value = order.customerId)) ?: return
        if (!customer.active) return
        runCatching {
            emails.send(
                to = customer.contact.email.value,
                subject = "Reparo extra aguardando aprovacao",
                body = "Ola, ${customer.name.value}.\n\n" +
                    "Ordem ${order.id.value}: reparo extra ${extra.name.value} " +
                    "(R$ ${extra.basePrice}) aguarda sua aprovacao.",
            )
        }.onFailure { error ->
            log.warn("Failed to send extra service email for order {}", extra.serviceOrderId, error)
        }
    }
}
