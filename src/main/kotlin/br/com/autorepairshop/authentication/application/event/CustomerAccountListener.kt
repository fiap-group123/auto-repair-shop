package br.com.autorepairshop.authentication.application.event

import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.customer.domain.event.CustomerDeactivated
import br.com.autorepairshop.customer.domain.event.CustomerReactivated
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CustomerAccountListener(private val users: UserRepository) {

    @EventListener
    @Transactional
    fun on(event: CustomerDeactivated) {
        val user = users.findByCustomerId(customerId = event.customerId) ?: return
        if (user.active) {
            user.deactivate()
            users.save(user = user)
        }
    }

    @EventListener
    @Transactional
    fun on(event: CustomerReactivated) {
        val user = users.findByCustomerId(customerId = event.customerId) ?: return
        if (!user.active) {
            user.reactivate()
            users.save(user = user)
        }
    }
}
