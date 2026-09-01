package br.com.autorepairshop.serviceorder.application.event

import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderBudgetRejected
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderExecutionStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.mail.EmailSender
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ServiceOrderStatusMailListener(
    private val orders: ServiceOrderRepository,
    private val budgets: BudgetRepository,
    private val customers: CustomerRepository,
    private val emails: EmailSender,
) {
    private val log = LoggerFactory.getLogger(ServiceOrderStatusMailListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderOpened) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.RECEIVED)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: DiagnosisStarted) {
        notify(
            serviceOrderId = ServiceOrderId(value = event.serviceOrderId),
            status = ServiceOrderStatus.IN_DIAGNOSIS,
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: DiagnosisFinished) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.WAITING_APPROVAL)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderApproved) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.BUDGET_APPROVED)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderBudgetRejected) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.BUDGET_REJECTED)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderExecutionStarted) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.IN_EXECUTION)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderCompleted) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.FINISHED)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ServiceOrderDelivered) {
        notify(serviceOrderId = event.serviceOrderId, status = ServiceOrderStatus.DELIVERED)
    }

    private fun notify(
        serviceOrderId: ServiceOrderId,
        status: ServiceOrderStatus,
    ) {
        val order = orders.findById(id = serviceOrderId) ?: return
        val customer = customers.findById(id = CustomerId(value = order.customerId)) ?: return
        if (!customer.active) return
        runCatching {
            emails.send(
                to = customer.contact.email.value,
                subject = subjectFor(status = status),
                body = bodyFor(
                    order = order,
                    customerName = customer.name.value,
                    status = status,
                ),
            )
        }.onFailure { error ->
            log.warn("Failed to send status email for service order {}", serviceOrderId.value, error)
        }
    }

    private fun subjectFor(status: ServiceOrderStatus): String = when (status) {
        ServiceOrderStatus.RECEIVED -> "Ordem de servico recebida"
        ServiceOrderStatus.IN_DIAGNOSIS -> "Diagnostico iniciado"
        ServiceOrderStatus.WAITING_APPROVAL -> "Orcamento aguardando aprovacao"
        ServiceOrderStatus.BUDGET_APPROVED -> "Orcamento aprovado"
        ServiceOrderStatus.BUDGET_REJECTED -> "Orcamento rejeitado"
        ServiceOrderStatus.IN_EXECUTION -> "Ordem de servico em execucao"
        ServiceOrderStatus.FINISHED -> "Servico concluido"
        ServiceOrderStatus.DELIVERED -> "Veiculo entregue"
    }

    private fun bodyFor(
        order: ServiceOrder,
        customerName: String,
        status: ServiceOrderStatus,
    ): String {
        val header = "Ola, $customerName.\n\nOrdem ${order.id.value}: ${subjectFor(status = status).lowercase()}."
        return if (status == ServiceOrderStatus.WAITING_APPROVAL) {
            val total = budgets.findByServiceOrderId(serviceOrderId = order.id.value)?.total
            "$header Total: R$ $total."
        } else {
            header
        }
    }
}
