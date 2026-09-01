package br.com.autorepairshop.accessidentity.application.event

import br.com.autorepairshop.accessidentity.application.usecase.IssueCustomerInviteUseCase
import br.com.autorepairshop.customer.domain.event.CustomerRegistered
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CustomerInviteEventListener(private val issueInvite: IssueCustomerInviteUseCase) {

    @EventListener
    fun on(event: CustomerRegistered) {
        issueInvite.execute(input = event.customerId)
    }
}
